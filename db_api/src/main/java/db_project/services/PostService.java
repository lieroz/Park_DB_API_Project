package db_project.services;

import db_project.models.*;
import db_project.services.queries.ForumQueries;
import db_project.services.queries.PostQueries;
import db_project.services.queries.ThreadQueries;
import db_project.services.queries.UserQueries;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by lieroz on 3.03.17.
 */
@Service
public class PostService {
    private final JdbcTemplate jdbcTemplate;

    public PostService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public final PostModel getPost(final Integer id) {
        return jdbcTemplate.queryForObject(PostQueries.getPostQuery(), new Object[]{id}, PostService::read);
    }

    public final PostDetailsModel getDetailedPostInfo(final Integer id, String[] related) {
        final PostModel post = getPost(id);
        UserViewModel user = null;
        ForumModel forum = null;
        ThreadModel thread = null;

        if (related != null) {

            for (String relation : related) {

                if (relation.equals("user")) {
                    user = jdbcTemplate.queryForObject(UserQueries.getUserQuery(), new Object[]{post.getAuthor(), null}, UserService::read);
                }

                if (relation.equals("forum")) {
                    forum = jdbcTemplate.queryForObject(ForumQueries.getForumQuery(), new Object[]{post.getForum()}, ForumService::read);
                }

                if (relation.equals("thread")) {
                    thread = jdbcTemplate.queryForObject(ThreadQueries.getThreadQuery(String.valueOf(post.getThread())),
                            new Object[]{post.getThread()}, ThreadService::read);
                }
            }
        }

        return new PostDetailsModel(user, forum, post, thread);
    }

    public final PostModel updatePost(final String message, final Integer id) {
        final PostModel post = getPost(id);
        final StringBuilder sql = new StringBuilder("UPDATE posts SET message = ?");

        if (!message.equals(post.getMessage())) {
            sql.append(", is_edited = TRUE");
            post.setIsEdited(true);
            post.setMessage(message);
        }

        sql.append(" WHERE id = ?");
        jdbcTemplate.update(sql.toString(), message, id);
        return post;
    }

    public static PostModel read(ResultSet rs, int rowNum) throws SQLException {
        final Timestamp timestamp = rs.getTimestamp("created");
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return new PostModel(
                rs.getString("nickname"),
                dateFormat.format(timestamp),
                rs.getString("slug"),
                rs.getInt("id"),
                rs.getBoolean("is_edited"),
                rs.getString("message"),
                rs.getInt("parent"),
                rs.getInt("thread_id")
        );
    }
}
