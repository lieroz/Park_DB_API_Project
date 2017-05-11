package db_project.JdbcDAO;

import db_project.DAO.PostDAO;
import db_project.Queries.ForumQueries;
import db_project.Queries.PostQueries;
import db_project.Queries.ThreadQueries;
import db_project.Queries.UserQueries;
import db_project.Views.*;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by lieroz on 9.05.17.
 */
@Component
public class JdbcPostDAO extends JdbcInferiorDAO implements PostDAO {
    public JdbcPostDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public final void create(final List<PostView> posts, final String slug_or_id) {
        final Integer threadId = slug_or_id.matches("\\d+") ? Integer.valueOf(slug_or_id) :
                getJdbcTemplate().queryForObject(ThreadQueries.getThreadId(), Integer.class, slug_or_id);
        final Integer forumId = getJdbcTemplate().queryForObject(ThreadQueries.getForumIdQuery(), Integer.class, threadId);
        final Timestamp created = new Timestamp(System.currentTimeMillis());
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try (Connection connection = getJdbcTemplate().getDataSource().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(PostQueries.createPostsQuery(), Statement.NO_GENERATED_KEYS);

            for (PostView post : posts) {
                final Integer postId = getJdbcTemplate().queryForObject("SELECT nextval('posts_id_seq')", Integer.class);
                preparedStatement.setString(1, post.getAuthor());
                preparedStatement.setTimestamp(2, created);
                preparedStatement.setInt(3, forumId);
                preparedStatement.setInt(4, postId);
                preparedStatement.setString(5, post.getMessage());
                preparedStatement.setInt(6, post.getParent());
                preparedStatement.setInt(7, threadId);
                preparedStatement.addBatch();
                post.setCreated(dateFormat.format(created));
                post.setId(postId);
            }

            preparedStatement.executeBatch();
            preparedStatement.close();
        } catch (SQLException ex) {
            throw new DataRetrievalFailureException(null);
        }
        getJdbcTemplate().update(ThreadQueries.updateForumsPostsCount(), posts.size(), forumId);
    }

    @Override
    public final PostView update(final String message, final Integer id) {
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
    public final PostDetailedView detailedView(final Integer id, final String[] related) {
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
    public final List<PostView> sort(final Integer limit, final Integer offset, final String sort,
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
    public final Integer count() {
        return getJdbcTemplate().queryForObject(PostQueries.countPostsQuery(), Integer.class);
    }

    @Override
    public final void clear() {
        getJdbcTemplate().execute(PostQueries.clearTableQuery());
    }
}