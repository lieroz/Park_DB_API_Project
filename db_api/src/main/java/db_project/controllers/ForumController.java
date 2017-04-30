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
            service.createForum(forum.getUser(), forum.getSlug(), forum.getTitle());

        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(service.getForum(forum.getSlug()));

        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(service.getForum(forum.getSlug()));
    }

    @RequestMapping(value = "/{slug}/create",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ThreadModel> createSlug(
            @RequestBody ThreadModel thread,
            @PathVariable(value = "slug") final String slug
    ) {
        final String threadSlug = thread.getSlug();

        try {
            thread = service.createThread(thread.getAuthor(), thread.getCreated(), slug,
                    thread.getMessage(), thread.getSlug(), thread.getTitle());

            if (thread == null) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(service.getThreadBySlug(threadSlug));

        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(thread);
    }

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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.status(HttpStatus.OK).body(forum);
    }

    @RequestMapping(value = "/{slug}/threads", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<List<ThreadModel>> viewThreads(
            @RequestParam(value = "limit", required = false, defaultValue = "100") final Integer limit,
            @RequestParam(value = "since", required = false) final String since,
            @RequestParam(value = "desc", required = false, defaultValue = "false") final Boolean desc,
            @PathVariable("slug") final String slug
    ) {
        try {
            final ForumModel forum = service.getForum(slug);

            if (forum == null) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(service.getForumThreadsInfo(slug, limit, since, desc));
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
            users = service.getForumUsersInfo(slug, limit, since, desc);
            final ForumModel forum = service.getForum(slug);

            if (forum == null) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.status(HttpStatus.OK).body(users);
    }
}
