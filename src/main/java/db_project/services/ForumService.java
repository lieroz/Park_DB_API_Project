package db_project.services;

import db_project.models.ForumModel;
import db_project.models.ForumSlugModel;
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

    public final List<ForumSlugModel> insertSlugIntoDb(final ForumSlugModel forumSlug) {
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
                ForumService::readForumSlug
        );
    }

    public final List<ForumModel> getForumInfo(final String slug) {
        return jdbcTemplate.query(
                "SELECT * FROM Forums WHERE slug = ?",
                new Object[]{slug},
                ForumService::readForum
        );
    }

    public final List<ForumSlugModel> getThreadsInfo(final String slug) {
        return jdbcTemplate.query(
                "SELECT * FROM ForumSlugs WHERE forum = ?",
                new Object[]{slug},
                ForumService::readForumSlug
        );
    }

    private static ForumModel readForum(ResultSet rs, int rowNum) throws SQLException {
        return new ForumModel(
                rs.getInt("posts"),
                rs.getString("user"),
                rs.getInt("threads"),
                rs.getString("slug"),
                rs.getString("title")
        );
    }

    private static ForumSlugModel readForumSlug(ResultSet rs, int rowNum) throws SQLException {
        return new ForumSlugModel(
                rs.getString("author"),
                rs.getString("created"),
                rs.getString("forum"),
                rs.getInt("id"),
                rs.getString("message"),
                rs.getString("slug"),
                rs.getString("title"),
                rs.getInt("votes")
                );
    }
}