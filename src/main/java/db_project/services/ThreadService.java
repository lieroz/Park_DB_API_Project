package db_project.services;

import db_project.models.PostModel;
import db_project.models.ThreadModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by lieroz on 27.02.17.
 */

/**
 * @brief Wrapper on JdbcTemplate for more convenient usage.
 */

@Service
public class ThreadService {
    /**
     * @brief Class used for communication with database.
     */
    private final JdbcTemplate jdbcTemplate;

    public ThreadService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * @brief Insert multiple posts into database by ID.
     */

    public final List<PostModel> insertPostsIntoDbById(final List<PostModel> posts, final Integer id) {
        for (PostModel post : posts) {

            if (post.getCreated() == null) {
                post.setCreated(LocalDateTime.now().toString());
            }

            Timestamp timestamp = Timestamp.valueOf(LocalDateTime.parse(post.getCreated(), DateTimeFormatter.ISO_DATE_TIME));

            if (!post.getCreated().endsWith("Z")) {
                timestamp = Timestamp.from(timestamp.toInstant().plusSeconds(-10800));
            }

            final String sql = "INSERT INTO posts (author, created, forum, \"message\", thread) " +
                    "VALUES(?, ?, (SELECT forum FROM threads WHERE id = ?), ?, ?)";

            jdbcTemplate.update(sql, post.getAuthor(), timestamp, id,
                    post.getMessage(), id);
        }

        return jdbcTemplate.query(
                "SELECT * FROM posts WHERE thread = ?",
                new Object[]{id},
                PostService::read
        );
    }

    /**
     * @brief Insert multiple posts into database by slug.
     */

    public final List<PostModel> insertPostsIntoDbBySlug(final List<PostModel> posts, final String slug) {
        for (PostModel post : posts) {

            if (post.getCreated() == null) {
                post.setCreated(LocalDateTime.now().toString());
            }

            Timestamp timestamp = Timestamp.valueOf(LocalDateTime.parse(post.getCreated(), DateTimeFormatter.ISO_DATE_TIME));

            if (!post.getCreated().endsWith("Z")) {
                timestamp = Timestamp.from(timestamp.toInstant().plusSeconds(-10800));
            }

            final String sql = "INSERT INTO posts (author, created, forum, \"message\", thread) " +
                    "VALUES(?, ?, (SELECT forum FROM threads WHERE LOWER(slug) = LOWER(?)), ?," +
                    "(SELECT id FROM threads WHERE LOWER(slug) = LOWER(?)))";

            jdbcTemplate.update(sql, post.getAuthor(), timestamp, slug,
                    post.getMessage(), slug);
        }

        return jdbcTemplate.query(
                "SELECT * FROM posts WHERE thread = (SELECT id FROM threads WHERE LOWER(slug) = LOWER(?))",
                new Object[]{slug},
                PostService::read
        );
    }

    /**
     * @brief Get all information about a specific thread from database.
     */

    // TODO issue with id here, where to find thread???
    public final List<ThreadModel> getThreadInfo(final String slug) {
        final StringBuilder sql = new StringBuilder("SELECT * FROM threads WHERE ");
        final Integer id;

        try {
            id  = Integer.valueOf(slug);

        } catch (NumberFormatException ex) {
            return jdbcTemplate.query(sql.append("LOWER(slug) = LOWER(?)").toString(),
                    new Object[]{slug}, ThreadService::read);
        }

        return jdbcTemplate.query(sql.append("id = ?").toString(),
                new Object[]{id}, ThreadService::read);
    }

    /**
     * @brief Serialize database row into ThreadModel object.
     */

    public static ThreadModel read(ResultSet rs, int rowNum) throws SQLException {
        final Timestamp timestamp = rs.getTimestamp("created");
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));

        return new ThreadModel(
                rs.getString("author"),
                dateFormat.format(timestamp),
                rs.getString("forum"),
                rs.getInt("id"),
                rs.getString("message"),
                rs.getString("slug"),
                rs.getString("title"),
                rs.getInt("votes")
        );
    }
}
