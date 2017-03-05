package db_project.services;

import db_project.models.ForumModel;
import db_project.models.ThreadModel;
import db_project.models.UserModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lieroz on 27.02.17.
 */

/**
 * @brief Wrapper on JdbcTemplate for more convenient usage.
 */

@Service
final public class ForumService {
    /**
     * @brief Class used for communication with database.
     */
    private final JdbcTemplate jdbcTemplate;

    public ForumService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * @brief Insert new forum into database.
     */

    public final void insertForumIntoDb(final ForumModel forum) {
        final String sql = "INSERT INTO forums (slug, title, \"user\") VALUES(?, ?, " +
                "(SELECT nickname FROM users WHERE LOWER(nickname) = LOWER(?)))";
        jdbcTemplate.update(sql, forum.getSlug(), forum.getTitle(), forum.getUser());
    }

    /**
     * @brief Insert new thread into database.
     */

    public final List<ThreadModel> insertThreadIntoDb(final ThreadModel thread) {
        if (thread.getCreated() == null) {
            thread.setCreated(LocalDateTime.now().toString());
        }

        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.parse(thread.getCreated(), DateTimeFormatter.ISO_DATE_TIME));

        if (!thread.getCreated().endsWith("Z")) {
            timestamp = Timestamp.from(timestamp.toInstant().plusSeconds(-10800));
        }

        final String sql = "INSERT INTO threads (author, created, forum, \"message\", " +
                "slug, title) VALUES(?, ?, " +
                "(SELECT slug FROM forums WHERE LOWER(slug) = LOWER(?)), " +
                "?, ?, ?)";

        jdbcTemplate.update(sql, thread.getAuthor(), timestamp, thread.getForum(),
                thread.getMessage(), thread.getSlug(), thread.getTitle()
        );

        return jdbcTemplate.query(
                "SELECT * FROM threads WHERE LOWER(slug) = LOWER(?)",
                new Object[]{thread.getSlug()},
                ThreadService::read
        );
    }

    /**
     * @brief Get information about forum.
     */

    public final List<ForumModel> getForumInfo(final String slug) {
        return jdbcTemplate.query(
                "SELECT * FROM forums WHERE LOWER(slug) = LOWER(?)",
                new Object[]{slug},
                ForumService::read
        );
    }

    /**
     * @brief Get information about a specific thread.
     */

    public final List<ThreadModel> getThreadInfo(final String slug) {
        return jdbcTemplate.query(
                "SELECT * FROM threads WHERE LOWER(slug) = LOWER(?)",
                new Object[]{slug},
                ThreadService::read
        );
    }

    /**
     * @brief Get information about all threads in a specific forum.
     */

    public final List<ThreadModel> getThreadsInfo(
            final String slug,
            final Integer limit,
            final String since,
            final Boolean desc
    ) {
        StringBuilder sql = new StringBuilder("SELECT * FROM threads WHERE forum = ?");
        final List<Object> args = new ArrayList<>();
        args.add(slug);

        if (since != null) {
            sql.append(" AND created ");

            if (desc == Boolean.TRUE) {
                sql.append("<= ?");

            } else {
                sql.append(">= ?");
            }

            args.add(Timestamp.valueOf(LocalDateTime.parse(since, DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        }

        sql.append(" ORDER BY created");

        if (desc == Boolean.TRUE) {
            sql.append(" DESC");
        }

        sql.append(" LIMIT ?");
        args.add(limit);

        return jdbcTemplate.query(
                sql.toString(),
                args.toArray(new Object[args.size()]),
                ThreadService::read
        );
    }

    /**
     * @brief Get information about all users in a specific forum.
     */

    public final List<UserModel> getUsersInfo(
            final String slug,
            final Integer limit,
            final String since,
            final Boolean desc
    ) {
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE LOWER(users.nickname) IN " +
                "(SELECT LOWER(posts.author) FROM posts WHERE posts.forum = ? " +
                "UNION " +
                "SELECT LOWER(threads.author) FROM threads WHERE threads.forum = ?)");
        final List<Object> args = new ArrayList<>();
        args.add(slug);
        args.add(slug);

        if (since != null) {
            sql.append(" AND users.id ");

            if (desc == Boolean.TRUE) {
                sql.append("< ");

            } else {
                sql.append("> ");
            }

            sql.append("(SELECT users.id FROM users WHERE LOWER(users.nickname) = LOWER(?))");
            args.add(since);
        }

        sql.append(" ORDER BY LOWER(users.nickname)"); // COLLATION HERE, but which??? ucs_basic COLLATION
        if (desc == Boolean.TRUE) {
            sql.append(" DESC");
        }

        sql.append(" LIMIT ?");
        args.add(limit);

        return jdbcTemplate.query(
                sql.toString(),
                args.toArray(new Object[args.size()]),
                UserService::read
        );
    }

    /**
     * @brief Serialize database row into ForumModel object.
     */

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