package db_project.controllers;

import db_project.models.ForumModel;
import db_project.models.ThreadModel;
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

@RestController
@RequestMapping(value = "api/forum")
public final class ForumController {
    private final JdbcTemplate jdbcTemplate;
    private final ForumService service;

    public ForumController(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.service = new ForumService(jdbcTemplate);
    }

    @RequestMapping(value = "/create",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ForumModel> createForum(
            @RequestBody final ForumModel forum
    ) {
        try {
            service.insertForumIntoDb(forum);

        } catch (DuplicateKeyException ex) {
            return new ResponseEntity<>(new ForumModel(service.getForumInfo(forum.getSlug()).get(0)), HttpStatus.CONFLICT);

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new ForumModel(service.getForumInfo(forum.getSlug()).get(0)), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{slug}/create",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ThreadModel> createSlug(
            @RequestBody ThreadModel thread,
            @PathVariable(value = "slug") final String slug
    ) {
        if (thread.getSlug() == null) {
            thread.setSlug(slug);
        }

        try {
            List<ThreadModel> threads = service.insertThreadIntoDb(thread);

            if (threads.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DuplicateKeyException ex) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (Objects.equals(thread.getSlug(), thread.getForum())) {
            thread.setSlug(null);
        }

        return new ResponseEntity<>(thread, HttpStatus.CREATED);
    }

    // TODO GET INFO ABOUT POSTS AND THREADS
    @RequestMapping(value = "/{slug}/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ForumModel> viewForum(
            @PathVariable("slug") final String slug
    ) {
        List<ForumModel> forums;

        try {
            forums = service.getForumInfo(slug);

            if (forums.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new ForumModel(forums.get(0)), HttpStatus.OK);
    }

    // TODO GET INFO ABOUT VOTES
    @RequestMapping(value = "/{slug}/threads", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<List<ThreadModel>> viewThreads(
            @RequestParam(value = "limit", required = false, defaultValue = "100") final Integer limit,
            @RequestParam(value = "since", required = false) final String since,
            @RequestParam(value = "desc", required = false) final Boolean desc,
            @PathVariable("slug") final String slug
    ) {
        try {
            List<ForumModel> forums = service.getForumInfo(slug);

            if (forums.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<ThreadModel> threads = service.getThreadsInfo(slug);

        return new ResponseEntity<>(threads, HttpStatus.OK);
    }
}
