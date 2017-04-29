package db_project.controllers;

import db_project.models.ForumModel;
import db_project.models.ThreadModel;
import db_project.models.UserViewModel;
import db_project.services.ForumService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * Created by lieroz on 27.02.17.
 */

/**
 * @brief Implementation of class that is responsible for handling all requests about forum.
 */

@RestController
@RequestMapping(value = "api/forum")
public final class ForumController {
    /**
     * @brief Class used for communication with database.
     */
    private final JdbcTemplate jdbcTemplate;
    /**
     * @brief Wrapper on JdbcTemplate for more convenient usage.
     */
    private final ForumService service;

    public ForumController(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.service = new ForumService(jdbcTemplate);
    }

    /**
     * @brief Create forum.
     */

    @RequestMapping(value = "/create",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ForumModel> createForum(
            @RequestBody final ForumModel forum
    ) {
        try {
            service.createForum(forum.getUser(), forum.getSlug(), forum.getTitle());

        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(service.getForum(forum.getSlug()));

        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(service.getForum(forum.getSlug()));
    }

    /**
     * @brief Create thread.
     * @brief {slug} stands for forum-slug here.
     */

    @RequestMapping(value = "/{slug}/create",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ThreadModel> createSlug(
            @RequestBody final ThreadModel thread,
            @PathVariable(value = "slug") final String slug
    ) {
        if (thread.getSlug() == null) {
            thread.setSlug(slug);
        }

        if (thread.getForum() == null) {
            thread.setForum(slug);
        }

        final List<ThreadModel> threads;

        try {
            threads = service.insertThreadIntoDb(thread);

            if (threads.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DuplicateKeyException ex) {
            return new ResponseEntity<>(service.getThreadInfo(thread.getSlug()).get(0), HttpStatus.CONFLICT);

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (Objects.equals(threads.get(0).getSlug(), threads.get(0).getForum())) {
            threads.get(0).setSlug(null);
        }

        return new ResponseEntity<>(threads.get(0), HttpStatus.CREATED);
    }

    /**
     * @brief Get all information about forum.
     * @brief {slug} stands for forum-slug here.
     */

    @RequestMapping(value = "/{slug}/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ForumModel> viewForum(
            @PathVariable("slug") final String slug
    ) {
        final ForumModel forum;

        try {
            forum = service.getForum(slug);

            if (forum == null) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(forum, HttpStatus.OK);
    }

    /**
     * @brief Get all threads from a forum.
     * @brief {slug} stands for forum-slug here.
     */

    @RequestMapping(value = "/{slug}/threads", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<List<ThreadModel>> viewThreads(
            @RequestParam(value = "limit", required = false, defaultValue = "100") final Integer limit,
            @RequestParam(value = "since", required = false) final String since,
            @RequestParam(value = "desc", required = false) final Boolean desc,
            @PathVariable("slug") final String slug
    ) {
        try {
            final ForumModel forum = service.getForum(slug);

            if (forum == null) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(service.getThreadsInfo(slug, limit, since, desc), HttpStatus.OK);
    }

    @RequestMapping(value = "/{slug}/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<List<UserViewModel>> viewUsers(
            @RequestParam(value = "limit", required = false, defaultValue = "100") final Integer limit,
            @RequestParam(value = "since", required = false) final String since,
            @RequestParam(value = "desc", required = false) final Boolean desc,
            @PathVariable("slug") final String slug
    ) {
        List<UserViewModel> users;

        try {
            users = service.getUsersInfo(slug, limit, since, desc);
            final ForumModel forum = service.getForum(slug);

            if (forum == null) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(users, HttpStatus.OK);
    }
}
