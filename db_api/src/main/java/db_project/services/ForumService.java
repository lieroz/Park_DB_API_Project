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
                                          @Nullable final String slug, @NotNull final String title) {
        if (created != null) {
            jdbcTemplate.update(ForumQueries.createThreadWithTimeQuery(), author, created, forum, message, slug, title);

        } else {
            jdbcTemplate.update(ForumQueries.createThreadWithoutTimeQuery(), author, forum, message, slug, title);
        }

        jdbcTemplate.update(ForumQueries.updateThreadsCount(), forum);
        return jdbcTemplate.queryForObject(ForumQueries.getThreadByTitleQuery(), new Object[]{title}, ThreadService::read);
    }

    public final ThreadModel getThreadBySlug(@NotNull final String slug) {
        return jdbcTemplate.queryForObject(ForumQueries.getThreadBySlugQuery(), new Object[]{slug}, ThreadService::read);
    }

    public final List<ThreadModel> getForumThreadsInfo(@NotNull final String slug, @NotNull final Integer limit,
            @Nullable final String since, @NotNull final Boolean desc) {
        final StringBuilder sql = new StringBuilder(ForumQueries.getThreadsByForumQuery());
        final List<Object> args = new ArrayList<>();
        args.add(slug);

        if (since != null) {
            sql.append(" AND t.created ");
            sql.append(desc == Boolean.TRUE ? "<= ?" : ">= ?");
            args.add(since);
        }

        sql.append(" ORDER BY t.created");

        if (desc == Boolean.TRUE) {
            sql.append(" DESC");
        }

        sql.append(" LIMIT ?");
        args.add(limit);
        return jdbcTemplate.query(sql.toString(), args.toArray(new Object[args.size()]), ThreadService::read);
    }

    public final List<UserViewModel> getForumUsersInfo(@NotNull final String slug, @NotNull final Integer limit,
            @Nullable final String since, @NotNull final Boolean desc) {
        final StringBuilder sql = new StringBuilder(ForumQueries.getUsersByForumQuery());
        final List<Object> args = new ArrayList<>();
        args.add(slug);

        if (since != null) {
            sql.append(" AND u.nickname ");
            sql.append(desc == Boolean.TRUE ? "< ?" : "> ?");
            args.add(since);
        }

        sql.append(" GROUP BY u.about, u.email, u.fullname, u.nickname");
        sql.append(" ORDER BY u.nickname COLLATE ucs_basic");

        if (desc == Boolean.TRUE) {
            sql.append(" DESC");
        }

        sql.append(" LIMIT ?");
        args.add(limit);
        return jdbcTemplate.query(sql.toString(), args.toArray(new Object[args.size()]), UserService::read);
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