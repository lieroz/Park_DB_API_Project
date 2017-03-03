package db_project.services;

import db_project.models.PostModel;
import db_project.models.ThreadModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
     * @brief Insert multiple posts into database.
     */

    public final List<PostModel> insertPostIntoDb(final List<PostModel> posts, final Integer id) {
        return posts;
    }

    /**
     * @brief Get all information about a specific thread from database.
     */

    // TODO issue with id here, where to find thread???
    public final List<ThreadModel> getThreadInfo(final String slug) {
        StringBuilder sql = new StringBuilder("SELECT * FROM threads WHERE ");
        Integer id;

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
        Timestamp timestamp = rs.getTimestamp("created");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));

        return new ThreadModel(
                rs.getString("author"),
                dateFormat.format(timestamp),
                rs.getString("forum"),
//                rs.getInt("id"),
                rs.getString("message"),
                rs.getString("slug"),
                rs.getString("title"),
                rs.getInt("votes")
        );
    }
}
