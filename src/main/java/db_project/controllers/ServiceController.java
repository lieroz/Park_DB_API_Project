package db_project.controllers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import db_project.models.ServiceModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lieroz on 4.03.17.
 */
@RestController
@RequestMapping("/api/service")
class ServiceController {
    /**
     * @brief Class used for communication with database.
     */
    private final JdbcTemplate jdbcTemplate;

    public ServiceController(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @RequestMapping("/status")
    public final ResponseEntity<Object> serverStatus() {
        final Integer forumsCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM forums", Integer.class);
        final Integer postsCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM posts", Integer.class);
        final Integer threadsCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM threads", Integer.class);
        final Integer usersCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);

        return new ResponseEntity<>(new ServiceModel(forumsCount, postsCount, threadsCount, usersCount), HttpStatus.OK);
    }
}
