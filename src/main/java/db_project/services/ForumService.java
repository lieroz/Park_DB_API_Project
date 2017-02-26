package db_project.services;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by lieroz on 27.02.17.
 */
public class ForumService {
    private final JdbcTemplate jdbcTemplate;

    public ForumService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
