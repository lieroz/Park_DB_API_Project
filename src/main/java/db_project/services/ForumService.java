package db_project.services;

import db_project.models.ForumModel;
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
}
