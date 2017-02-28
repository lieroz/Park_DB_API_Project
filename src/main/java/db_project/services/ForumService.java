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

    public final List<ThreadModel> insertSlugIntoDb(final ThreadModel forumSlug) {
        if (forumSlug.getCreated() == null) {
            forumSlug.setCreated(LocalDateTime.now().toString());
        }

        final String sql = "INSERT INTO ForumSlugs (" +
                "author, " +
                "created, " +
                "forum, " +
                "\"message\", " +
                "slug, " +
                "title) " +
                "VALUES(?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                forumSlug.getAuthor(),
                forumSlug.getCreated(),
                forumSlug.getForum(),
                forumSlug.getMessage(),
                forumSlug.getSlug(),
                forumSlug.getTitle()
        );

        return jdbcTemplate.query(
                "SELECT * FROM ForumSlugs WHERE slug = ?",
                new Object[]{forumSlug.getSlug()},
                ThreadService::readThread
        );
    }

    public final List<ForumModel> getForumInfo(final String slug) {
        return jdbcTemplate.query(
                "SELECT * FROM Forums WHERE slug = ?",
                new Object[]{slug},
                ForumService::readForum
        );
    }

    public final List<ThreadModel> getThreadsInfo(final String slug) {
        return jdbcTemplate.query(
                "SELECT * FROM ForumSlugs WHERE forum = ?",
                new Object[]{slug},
                ThreadService::readThread
        );
    }

    public static ForumModel readForum(ResultSet rs, int rowNum) throws SQLException {
        return new ForumModel(
                rs.getInt("posts"),
                rs.getString("user"),
                rs.getInt("threads"),
                rs.getString("slug"),
                rs.getString("title")
        );
    }
}