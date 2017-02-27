package db_project.controllers;

import db_project.models.ForumModel;
import db_project.models.ForumSlugModel;
import db_project.services.ForumService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Constructor;

/**
 * Created by lieroz on 27.02.17.
 */

@RestController
@RequestMapping(value = "api/forum")
public final class ForumController {
    private final JdbcTemplate jdbcTemplate;
    private final ForumService forumService;

    public ForumController(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.forumService = new ForumService(jdbcTemplate);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ForumModel> createForum(
            @RequestBody final ForumModel forum
    ) {
        try {
            forumService.insertForumIntoDb(forum);

        } catch (DuplicateKeyException ex) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new ForumModel(forum), HttpStatus.CREATED);
    }

    @RequestMapping("/{slug}/create")
    public final ResponseEntity<ForumSlugModel> createSlug(
            @RequestBody ForumSlugModel forumSlug,
            @PathVariable(value = "slug") final String slug
    ) {
        forumSlug.setSlug(slug);

        try {
            forumService.insertSlugIntoDb(forumSlug);

        } catch (DuplicateKeyException ex) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new ForumSlugModel(forumSlug), HttpStatus.CREATED);
    }
}
