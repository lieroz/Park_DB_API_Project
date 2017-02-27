package db_project.services;

import db_project.models.ForumModel;
import db_project.models.ForumSlugModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

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
        final String sql = "INSERT INTO Forums (slug, title, author) VALUES(?, ?, ?)";
        jdbcTemplate.update(sql, forum.getSlug(), forum.getTitle(), forum.getUser());
    }

    public final void insertSlugIntoDb(final ForumSlugModel forumSlug) {
        final String sql = "INSERT INTO ForumSlugs (author, forum, slug, title) VALUES(?, ?, ?, ?)";
        jdbcTemplate.update(sql, forumSlug.getAuthor(), forumSlug.getForum(), forumSlug.getSlug(), forumSlug.getTitle());
    }
}
