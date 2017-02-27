package db_project.services;

import db_project.models.ForumModel;
import db_project.models.ForumSlugModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
        final String sql = "INSERT INTO Forums (slug, title, \"user\") VALUES(?, ?, ?)";
        jdbcTemplate.update(sql, forum.getSlug(), forum.getTitle(), forum.getUser());
    }

    public final void insertSlugIntoDb(final ForumSlugModel forumSlug) {
        if (forumSlug.getCreated() == null) {
            forumSlug.setCreated(LocalDateTime.now().toString());
        }

        final String sql = "INSERT INTO ForumSlugs (author, created, forum, \"message\", slug, title) VALUES(?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, forumSlug.getAuthor(), forumSlug.getCreated(), forumSlug.getForum(), forumSlug.getMessage(),
                forumSlug.getSlug(), forumSlug.getTitle());
    }
}
