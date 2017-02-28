package db_project.controllers;

import db_project.models.ThreadModel;
import db_project.services.ThreadService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lieroz on 27.02.17.
 */

@RestController
@RequestMapping(value = "/api/thread/{slug_or_id}")
public class ThreadController {
    private final JdbcTemplate jdbcTemplate;
    private final ThreadService service;

    public ThreadController(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.service = new ThreadService(jdbcTemplate);
    }


}
