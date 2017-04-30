package db_project.services;

import db_project.models.PostModel;
import db_project.models.ThreadModel;
import db_project.models.VoteModel;
import db_project.services.queries.ThreadQueries;
import db_project.views.ForumSimpleView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lieroz on 27.02.17.
 */
@Service
public class ThreadService {
    private final JdbcTemplate jdbcTemplate;

    public ThreadService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static Integer postId = 0;

    @Transactional
    public final void createPosts(final List<PostModel> posts, final String slug_or_id) {
        final Integer threadId = slug_or_id.matches("\\d+") ? Integer.valueOf(slug_or_id) :
                jdbcTemplate.queryForObject(ThreadQueries.getThreadId(), Integer.class, slug_or_id);
        final ForumSimpleView forumSimpleView = jdbcTemplate.queryForObject(ThreadQueries.getForumIdAndSlugQuery(slug_or_id),
                new Object[]{slug_or_id}, (rs, rowNum) -> new ForumSimpleView(rs.getString("slug"), rs.getInt("id")));
        final Timestamp created = new Timestamp(System.currentTimeMillis());
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        for (PostModel post : posts) {
            post.setCreated(dateFormat.format(created));
            post.setForum(forumSimpleView.getForumSlug());
            post.setId(++postId);
            post.setThread(threadId);

            jdbcTemplate.update(ThreadQueries.createPostsQuery(), post.getAuthor(), created, forumSimpleView.getForumId(),
                    post.getMessage(), post.getParent(), threadId);
        }

        jdbcTemplate.update(ThreadQueries.updateForumsPostsCount(), posts.size(), forumSimpleView.getForumId());
    }

    public final ThreadModel getThreadInfo(final String slug_or_id) {
        return jdbcTemplate.queryForObject(ThreadQueries.getThreadQuery(slug_or_id), new Object[]{slug_or_id}, ThreadService::read);
    }

    public final void updateThreadInfoFromDb(final ThreadModel thread, final String slug) {
        final StringBuilder sql = new StringBuilder("UPDATE threads SET");
        final List<Object> args = new ArrayList<>();

        if (thread.getMessage() != null && !thread.getMessage().isEmpty()) {
            sql.append(" message = ?,");
            args.add(thread.getMessage());
        }

        if (thread.getTitle() != null && !thread.getTitle().isEmpty()) {
            sql.append(" title = ?,");
            args.add(thread.getTitle());
        }

        if (args.isEmpty()) {
            return;
        }

        sql.delete(sql.length() - 1, sql.length());
        final Integer id;

        try {
            id = Integer.valueOf(slug);
            sql.append(" WHERE id = ?");
            args.add(id);

        } catch (NumberFormatException ex) {
            sql.append(" WHERE LOWER(slug) = LOWER(?)");
            args.add(slug);
        }

        jdbcTemplate.update(sql.toString(), args.toArray());
    }

    @Transactional
    public final ThreadModel updateThreadVotes(final VoteModel vote, final String slug_or_id) {
        final Integer threadId = slug_or_id.matches("\\d+") ? Integer.valueOf(slug_or_id) :
                jdbcTemplate.queryForObject(ThreadQueries.getThreadId(), Integer.class, slug_or_id);
        jdbcTemplate.update(ThreadQueries.updateUserVoteQuery(), threadId, vote.getVoice(), vote.getNickname());
        jdbcTemplate.update(ThreadQueries.updateThreadVotesQuery(), threadId, threadId);
        return jdbcTemplate.queryForObject(ThreadQueries.getThreadQuery(slug_or_id), new Object[]{slug_or_id}, ThreadService::read);
    }

    public final List<PostModel> getSortedPosts(final Integer limit, final Integer offset, final String sort,
                                                final Boolean desc, final String slug_or_id) {
        switch (sort) {

            case "flat": {
                return jdbcTemplate.query(ThreadQueries.postsFlatSortQuery(slug_or_id, desc),
                        new Object[]{slug_or_id, limit, offset}, PostService::read);
            }

            case "tree": {
                return jdbcTemplate.query(ThreadQueries.postsTreeSortQuery(slug_or_id, desc),
                        new Object[]{slug_or_id, limit, offset}, PostService::read);
            }

            case "parent_tree": {
                return jdbcTemplate.query(ThreadQueries.postsParentTreeSortQuery(slug_or_id, desc),
                        new Object[]{slug_or_id, limit, offset}, PostService::read);
            }

            default: {
                throw new NullPointerException();
            }
        }
    }

    public static ThreadModel read(ResultSet rs, int rowNum) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("created");
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return new ThreadModel(
                rs.getString("nickname"),
                dateFormat.format(timestamp.getTime()),
                rs.getString("f_slug"),
                rs.getInt("id"),
                rs.getString("message"),
                rs.getString("t_slug"),
                rs.getString("title"),
                rs.getInt("votes")
        );
    }
}
