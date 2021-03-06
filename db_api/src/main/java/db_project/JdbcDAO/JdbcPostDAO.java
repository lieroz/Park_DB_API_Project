package db_project.JdbcDAO;

import db_project.DAO.PostDAO;
import db_project.Queries.ForumQueries;
import db_project.Queries.PostQueries;
import db_project.Queries.ThreadQueries;
import db_project.Queries.UserQueries;
import db_project.Views.*;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by lieroz on 9.05.17.
 */
@Service
public class JdbcPostDAO extends JdbcInferiorDAO implements PostDAO {
    public JdbcPostDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void create(final List<PostView> posts, final String slug_or_id) {
        final Integer threadId = slug_or_id.matches("\\d+") ? Integer.valueOf(slug_or_id) :
                getJdbcTemplate().queryForObject(ThreadQueries.getThreadId(), Integer.class, slug_or_id);
        final Integer forumId = getJdbcTemplate().queryForObject(ThreadQueries.getForumIdQuery(), Integer.class, threadId);
        final Timestamp created = new Timestamp(System.currentTimeMillis());
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try (Connection connection = getJdbcTemplate().getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement postsPrepared = connection.prepareStatement(PostQueries.createPostsQuery(), Statement.NO_GENERATED_KEYS);
                 PreparedStatement userForumsPrepared = connection.prepareStatement(PostQueries.insertIntoForumUsers(), Statement.NO_GENERATED_KEYS)) {
                for (PostView post : posts) {
                    final Integer userId = getJdbcTemplate().queryForObject(UserQueries.findUserIdQuery(), Integer.class, post.getAuthor());
                    final Integer postId = getJdbcTemplate().queryForObject("SELECT nextval('posts_id_seq')", Integer.class);
                    postsPrepared.setInt(1, userId);
                    postsPrepared.setTimestamp(2, created);
                    postsPrepared.setInt(3, forumId);
                    postsPrepared.setInt(4, postId);
                    postsPrepared.setString(5, post.getMessage());
                    postsPrepared.setInt(6, post.getParent());
                    postsPrepared.setInt(7, threadId);
                    postsPrepared.setInt(9, postId);
                    if (post.getParent() == 0) {
                        postsPrepared.setArray(8, null);
                        postsPrepared.setInt(10, postId);
                    } else {
                        final Array path = getJdbcTemplate().queryForObject("SELECT path FROM posts WHERE id = ?", Array.class, post.getParent());
                        postsPrepared.setArray(8, path);
                        postsPrepared.setInt(10, ((Integer[]) path.getArray())[0]);
                    }
                    postsPrepared.addBatch();
                    userForumsPrepared.setInt(1, userId);
                    userForumsPrepared.setInt(2, forumId);
                    userForumsPrepared.addBatch();
                    post.setCreated(dateFormat.format(created));
                    post.setId(postId);
                }
                postsPrepared.executeBatch();
                userForumsPrepared.executeBatch();
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw new DataRetrievalFailureException(null);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new DataRetrievalFailureException(null);
        }
        getJdbcTemplate().update(ThreadQueries.updateForumsPostsCount(), posts.size(), forumId);
    }

    @Override
    public PostView update(final String message, final Integer id) {
        final PostView post = findById(id);
        final StringBuilder sql = new StringBuilder("UPDATE posts SET message = ?");
        if (!message.equals(post.getMessage())) {
            sql.append(", is_edited = TRUE");
            post.setIsEdited(true);
            post.setMessage(message);
        }
        sql.append(" WHERE id = ?");
        getJdbcTemplate().update(sql.toString(), message, id);
        return post;
    }

    @Override
    public PostView findById(final Integer id) {
        return getJdbcTemplate().queryForObject(PostQueries.getPostQuery(), new Object[]{id}, readPost);
    }

    @Override
    public PostDetailedView detailedView(final Integer id, final String[] related) {
        final PostView post = findById(id);
        UserView user = null;
        ForumView forum = null;
        ThreadView thread = null;
        if (related != null) {
            for (String relation : related) {
                switch (relation) {
                    case "user":
                        user = getJdbcTemplate().queryForObject(UserQueries.findUserQuery(),
                                new Object[]{post.getAuthor(), null}, readUser);
                        break;
                    case "forum":
                        forum = getJdbcTemplate().queryForObject(ForumQueries.getForumQuery(),
                                new Object[]{post.getForum()}, readForum);
                        break;
                    case "thread":
                        thread = getJdbcTemplate().queryForObject(ThreadQueries.getThreadQuery(String.valueOf(post.getThread())),
                                new Object[]{post.getThread()}, readThread);
                }
            }
        }
        return new PostDetailedView(user, forum, post, thread);
    }

    @Override
    public List<PostView> sort(final ThreadView thread, final String slug_or_id, final Integer limit, final Integer since, final String sort, final Boolean desc) {
        List<Object> arguments = new ArrayList<>();
        arguments.add(thread.getId());
        if (since != null) {
            arguments.add(since);
        }
        if (limit != null) {
            arguments.add(limit);
        }
        if (sort == null) {
            return getJdbcTemplate().query(PostQueries.getPostsFlat(limit, since, desc), arguments.toArray(), readPost);
        }
        switch (sort) {
            case "flat" :
                return getJdbcTemplate().query(PostQueries.getPostsFlat(limit, since, desc), arguments.toArray(), readPost);
            case "tree" :
                return getJdbcTemplate().query(PostQueries.getPostsTree(limit,since,desc), arguments.toArray(), readPost);
            case "parent_tree" :
                return getJdbcTemplate().query(PostQueries.getPostsParentTree(limit, since, desc), arguments.toArray(), readPost);
            default:
                break;
        }
        return getJdbcTemplate().query(PostQueries.getPostsFlat(limit, since, desc), arguments.toArray(), readPost);
    }

    @Override
    public Integer count() {
        return getJdbcTemplate().queryForObject(PostQueries.countPostsQuery(), Integer.class);
    }

    @Override
    public void clear() {
        getJdbcTemplate().execute(PostQueries.clearTableQuery());
    }
}
