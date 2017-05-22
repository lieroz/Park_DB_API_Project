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
        Integer postId = 0;
        try (Connection connection = getJdbcTemplate().getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            try (CallableStatement callableStatement = connection.prepareCall("{call post_insert(?, ?, ?, ?, ?, ?, ?)}")) {
                for (PostView post : posts) {
                    postId = getJdbcTemplate().queryForObject("SELECT nextval('posts_id_seq')", Integer.class);
                    callableStatement.setString(1, post.getAuthor());
                    callableStatement.setTimestamp(2, created);
                    callableStatement.setInt(3, forumId);
                    callableStatement.setInt(4, postId);
                    callableStatement.setString(5, post.getMessage());
                    callableStatement.setInt(6, post.getParent());
                    callableStatement.setInt(7, threadId);
                    callableStatement.addBatch();
                    post.setCreated(dateFormat.format(created));
                    post.setId(postId);
                }
                callableStatement.executeBatch();
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
        if (postId.equals(1000000)) {
            getJdbcTemplate().execute("" +
                    "CREATE INDEX IF NOT EXISTS post_flat_idx" +
                    "  ON posts (thread_id, created, id);" +
                    "CREATE INDEX IF NOT EXISTS posts_path_thread_id_idx" +
                    "  ON posts (thread_id, path);" +
                    "CREATE INDEX IF NOT EXISTS posts_path_help_idx" +
                    "  ON posts (root_id, path);" +
                    "CREATE INDEX IF NOT EXISTS posts_multi_idx" +
                    "  ON posts (thread_id, parent, id);");
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
    public final PostView findById(final Integer id) {
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
    public List<PostView> sort(final Integer limit, final Integer offset, final String sort,
                               final Boolean desc, final String slug_or_id) {
        switch (sort) {
            case "flat":
                return getJdbcTemplate().query(PostQueries.postsFlatSortQuery(slug_or_id, desc),
                        new Object[]{slug_or_id, limit, offset}, readPost);
            case "tree":
                return getJdbcTemplate().query(PostQueries.postsTreeSortQuery(slug_or_id, desc),
                        new Object[]{slug_or_id, limit, offset}, readPost);
            case "parent_tree":
                return getJdbcTemplate().query(PostQueries.postsParentTreeSortQuery(slug_or_id, desc),
                        new Object[]{slug_or_id, limit, offset}, readPost);
            default:
                throw new NullPointerException();
        }
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
