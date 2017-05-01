package db_project.services;

import db_project.models.ForumModel;
import db_project.models.ThreadModel;
import db_project.models.UserViewModel;
import db_project.services.queries.ForumQueries;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lieroz on 27.02.17.
 */
@Service
final public class ForumService {
    private final JdbcTemplate jdbcTemplate;
    private static Integer threadId = 0;

    public ForumService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public final void createForum(final String userNickname, final String slug, final String title) {
        jdbcTemplate.update(ForumQueries.createForumQuery(), userNickname, slug, title);
    }

    public final ForumModel getForum(final String slug) {
        return jdbcTemplate.queryForObject(ForumQueries.getForumQuery(), new Object[]{slug}, ForumService::read);
    }

    public final ThreadModel createThread(final String author, final String created, final String forum, final String message,
                                          final String slug, final String title) {
        ++threadId;

        if (created != null) {
            jdbcTemplate.update(ForumQueries.createThreadWithTimeQuery(), author, created, forum, message, slug, title);

        } else {
            jdbcTemplate.update(ForumQueries.createThreadWithoutTimeQuery(), author, forum, message, slug, title);
        }

        jdbcTemplate.update(ForumQueries.updateThreadsCount(), forum);
        return jdbcTemplate.queryForObject(ForumQueries.getThreadByIdQuery(), new Object[]{threadId}, ThreadService::read);
    }

    public final ThreadModel getThreadBySlug(final String slug) {
        return jdbcTemplate.queryForObject(ForumQueries.getThreadBySlugQuery(), new Object[]{slug}, ThreadService::read);
    }

    public final List<ThreadModel> getForumThreadsInfo(final String slug, final Integer limit, final String since, final Boolean desc) {
        final StringBuilder sql = new StringBuilder(ForumQueries.getThreadsByForumQuery());
        final List<Object> args = new ArrayList<>();
        args.add(slug);

        if (since != null) {
            sql.append(" AND t.created ");
            sql.append(desc == Boolean.TRUE ? "<= ?" : ">= ?");
            args.add(since);
        }

        sql.append(" ORDER BY t.created");
        sql.append(desc == Boolean.TRUE ? " DESC" : "");
        sql.append(" LIMIT ?");
        args.add(limit);

        return jdbcTemplate.query(sql.toString(), args.toArray(new Object[args.size()]), ThreadService::read);
    }

    public final List<UserViewModel> getForumUsersInfo(final String slug, final Integer limit, final String since, final Boolean desc) {
        final StringBuilder sql = new StringBuilder(ForumQueries.getUsersByForumQuery());
        final List<Object> args = new ArrayList<>();
        args.add(slug);

        if (since != null) {
            sql.append(" AND u.nickname ");
            sql.append(desc == Boolean.TRUE ? "< ?" : "> ?");
            args.add(since);
        }

        sql.append(" GROUP BY u.about, u.email, u.fullname, u.nickname");
        sql.append(" ORDER BY u.nickname COLLATE ucs_basic");
        sql.append(desc == Boolean.TRUE ? " DESC" : "");
        sql.append(" LIMIT ?");
        args.add(limit);

        return jdbcTemplate.query(sql.toString(), args.toArray(new Object[args.size()]), UserService::read);
    }

    public static ForumModel read(ResultSet rs, int rowNum) throws SQLException {
        return new ForumModel(
                rs.getInt("posts"),
                rs.getString("slug"),
                rs.getInt("threads"),
                rs.getString("title"),
                rs.getString("nickname")
        );
    }
}