package db_project.services;

import db_project.models.ForumModel;
import db_project.models.ThreadModel;
import db_project.models.UserViewModel;
import db_project.services.queries.ForumQueries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

@Service
final public class ForumService {
    private final JdbcTemplate jdbcTemplate;

    public ForumService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public final void createForum(@NotNull final String userNickname,
                                  @NotNull final String slug, @NotNull final String title) {
        jdbcTemplate.update(ForumQueries.createForumQuery(), userNickname, slug, title);
    }

    public final ForumModel getForum(@NotNull final String slug) {
        return jdbcTemplate.queryForObject(ForumQueries.getForumQuery(), new Object[]{slug}, ForumService::read);
    }

    public final ThreadModel createThread(@NotNull final String author, @Nullable final String created,
                                          @NotNull final String forum, @NotNull final String message,
                                          @NotNull final String slug, @NotNull final String title) {
        jdbcTemplate.update(ForumQueries.createThreadQuery(), author, created, forum, message, slug, title);
        jdbcTemplate.update(ForumQueries.updateThreadsCount(), forum);

        return jdbcTemplate.queryForObject(ForumQueries.getThreadQuery(), new Object[]{slug}, ThreadService::read);
    }

    public final ThreadModel getThread(@NotNull final String slug) {
        return jdbcTemplate.queryForObject(ForumQueries.getThreadQuery(), new Object[]{slug}, ThreadService::read);
    }

    public final List<ThreadModel> getThreadsInfo(
            final String slug,
            final Integer limit,
            final String since,
            final Boolean desc
    ) {
        StringBuilder sql = new StringBuilder("SELECT * FROM threads WHERE LOWER(forum) = LOWER(?)");
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

    public final List<UserViewModel> getUsersInfo(
            final String slug,
            final Integer limit,
            final String since,
            final Boolean desc
    ) {
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE LOWER(users.nickname) IN " +
                "(SELECT LOWER(posts.author) FROM posts WHERE LOWER(posts.forum) = LOWER(?) " +
                "UNION " +
                "SELECT LOWER(threads.author) FROM threads WHERE LOWER(threads.forum) = LOWER(?))");
        final List<Object> args = new ArrayList<>();
        args.add(slug);
        args.add(slug);

        if (since != null) {
            sql.append(" AND LOWER(users.nickname) ");

            if (desc == Boolean.TRUE) {
                sql.append("< LOWER(?)");

            } else {
                sql.append("> LOWER(?)");
            }

            args.add(since);
        }

        sql.append(" ORDER BY LOWER(users.nickname) COLLATE ucs_basic");

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

    public static ForumModel read(ResultSet rs, int rowNum) throws SQLException {
        return new ForumModel(
                rs.getInt("posts"),
                rs.getString("slug"),
                rs.getInt("threads"),
                rs.getString("title"),
                rs.getString("nickname")
        );
    }
}