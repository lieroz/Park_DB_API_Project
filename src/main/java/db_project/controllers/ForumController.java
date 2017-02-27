package db_project.controllers;

import db_project.models.ForumModel;
import db_project.models.ForumSlugModel;
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
            return new ResponseEntity<>(HttpStatus.CONFLICT);

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new ForumModel(forum), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{slug}/create",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ForumSlugModel> createSlug(
            @RequestBody ForumSlugModel forumSlug,
            @PathVariable(value = "slug") final String slug
    ) {
        forumSlug.setSlug(slug);
        List<ForumSlugModel> slugs;

        try {
            slugs = service.insertSlugIntoDb(forumSlug);

            if (slugs.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DuplicateKeyException ex) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(slugs.get(0), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{slug}/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ForumModel> viewForum(
            @PathVariable("slug") final String slug
    ) {
        List<ForumModel> forum;

        try {
            forum = service.getForumInfo(slug);

            if (forum.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new ForumModel(forum.get(0)), HttpStatus.OK);
    }

    @RequestMapping(value = "/{slug}/threads", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<List<ForumSlugModel>> viewThreads(
            @PathVariable("slug") final String slug
    ) {
        try {
            List<ForumModel> forum = service.getForumInfo(slug);

            if (forum.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<ForumSlugModel> threads = service.getThreadsInfo(slug);

        return new ResponseEntity<>(threads, HttpStatus.OK);
    }
}
