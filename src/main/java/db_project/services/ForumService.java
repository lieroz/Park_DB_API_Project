package db_project.services;

import db_project.models.ForumModel;
import db_project.models.ForumSlugModel;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by lieroz on 27.02.17.
 */

final public class ForumService {
    private final JdbcTemplate jdbcTemplate;

    public ForumService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public final void insertForumIntoDb(final ForumModel forum) {
        final String sql = "INSERT INTO Forums (posts, slug, threads, title, author) VALUES(?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, forum.getPosts(), forum.getSlug(), forum.getThreads(), forum.getTitle(), forum.getUser());
    }

    public final void insertSlugIntoDb(final ForumSlugModel forumSlug) {
        final String sql = "INSERT INTO ForumSlugs (author, created, id, msg, slug, title, votes) VALUES(?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, forumSlug.getAuthor(), forumSlug.getCreated(), forumSlug.getId(), forumSlug.getMessage(),
                forumSlug.getSlug(), forumSlug.getTitle(), forumSlug.getVotes());
    }
}
