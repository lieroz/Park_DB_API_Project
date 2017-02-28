package db_project.services;

import db_project.models.ForumModel;
import db_project.models.ThreadModel;
import db_project.models.UserModel;
import org.springframework.dao.EmptyResultDataAccessException;
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
        List<UserModel> users = jdbcTemplate.query(
                "SELECT * FROM Users WHERE LOWER(nickname) = LOWER(?)",
                new Object[]{forum.getUser()},
                UserService::read);

        if (users.isEmpty()) {
            throw new EmptyResultDataAccessException(0);
        }

        final String sql = "INSERT INTO Forums (" +
                "slug, " +
                "title, " +
                "\"user\") " +
                "VALUES(?, ?, ?)";
        jdbcTemplate.update(
                sql,
                forum.getSlug(),
                forum.getTitle(),
                users.get(0).getNickname()
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
                "SELECT * FROM Forums WHERE LOWER(slug) = LOWER(?)",
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
                rs.getString("slug"),
                rs.getInt("threads"),
                rs.getString("title"),
                rs.getString("user")
        );
    }
}