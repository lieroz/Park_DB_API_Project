package db_project.services;

import db_project.models.ForumModel;
import db_project.models.ThreadModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
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
        final String sql = "INSERT INTO Forums (" +
                "slug, " +
                "title, " +
                "\"user\") " +
                "VALUES(?, ?, ?)";
        jdbcTemplate.update(
                sql,
                forum.getSlug(),
                forum.getTitle(),
                forum.getUser()
        );
    }

    public final List<ThreadModel> insertThreadIntoDb(final ThreadModel thread) {
        if (thread.getCreated() == null) {
            thread.setCreated(LocalDateTime.now().toString());
        }

        final String sql = "INSERT INTO Threads (" +
                "author, " +
                "created, " +
                "forum, " +
                "\"message\", " +
                "slug, " +
                "title) " +
                "VALUES(?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                thread.getAuthor(),
                thread.getCreated(),
                thread.getForum(),
                thread.getMessage(),
                thread.getSlug(),
                thread.getTitle()
        );

        return jdbcTemplate.query(
                "SELECT * FROM Threads WHERE slug = ?",
                new Object[]{thread.getSlug()},
                ThreadService::read
        );
    }

    public final List<ForumModel> getForumInfo(final String slug) {
        return jdbcTemplate.query(
                "SELECT * FROM Forums WHERE slug = ?",
                new Object[]{slug},
                ForumService::read
        );
    }

    public final List<ThreadModel> getThreadsInfo(final String slug) {
        return jdbcTemplate.query(
                "SELECT * FROM Threads WHERE forum = ?",
                new Object[]{slug},
                ThreadService::read
        );
    }

    public static ForumModel read(ResultSet rs, int rowNum) throws SQLException {
        return new ForumModel(
                rs.getInt("posts"),
                rs.getString("user"),
                rs.getInt("threads"),
                rs.getString("slug"),
                rs.getString("title")
        );
    }
}