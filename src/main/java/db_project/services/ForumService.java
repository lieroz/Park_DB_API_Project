package db_project.services;

import db_project.models.ForumModel;
import db_project.models.ThreadModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Created by lieroz on 27.02.17.
 */

@Service
final public class ForumService {
    private final JdbcTemplate jdbcTemplate;

    public ForumService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public final void insertForumIntoDb(final ForumModel forum) {
        final String sql = "INSERT INTO forums (slug, title, \"user\") VALUES(?, ?, " +
                "(SELECT nickname FROM users WHERE LOWER(nickname) = LOWER(?)))";
        jdbcTemplate.update(sql, forum.getSlug(), forum.getTitle(), forum.getUser());
    }

    public final List<ThreadModel> insertThreadIntoDb(final ThreadModel thread) {
        if (thread.getCreated() == null) {
            thread.setCreated(LocalDateTime.now().toString());
        }

        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.parse(thread.getCreated(), DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        final String sql = "INSERT INTO threads (author, created, forum, \"message\", " +
                "slug, title) VALUES(?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql, thread.getAuthor(), timestamp, thread.getForum(),
                thread.getMessage(), thread.getSlug(), thread.getTitle()
        );

        return jdbcTemplate.query(
                "SELECT * FROM threads WHERE slug = ?",
                new Object[]{thread.getSlug()},
                ThreadService::read
        );
    }

    public final List<ForumModel> getForumInfo(final String slug) {
        return jdbcTemplate.query(
                "SELECT * FROM forums WHERE LOWER(slug) = LOWER(?)",
                new Object[]{slug},
                ForumService::read
        );
    }

    public final List<ThreadModel> getThreadsInfo(final String slug) {
        return jdbcTemplate.query(
                "SELECT * FROM threads WHERE forum = ?",
                new Object[]{slug},
                ThreadService::read
        );
    }

    public static ForumModel read(ResultSet rs, int rowNum) throws SQLException {
        return new ForumModel(
                rs.getInt("posts"),
                rs.getString("slug"),
                rs.getInt("threads"),
                rs.getString("title"),
                rs.getString("user")
        );
    }
}