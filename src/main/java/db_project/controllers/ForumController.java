package db_project.controllers;

import db_project.services.ForumService;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by lieroz on 27.02.17.
 */
public class ForumController {
    private final JdbcTemplate jdbcTemplate;
    private final ForumService service;

    public ForumController(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.service = new ForumService(jdbcTemplate);
    }
}
