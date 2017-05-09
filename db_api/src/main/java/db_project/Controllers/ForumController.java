package db_project.Controllers;

import db_project.Views.ForumView;
import db_project.Views.ThreadView;
import db_project.Views.UserView;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by lieroz on 27.02.17.
 */
@RestController
@RequestMapping(value = "api/forum")
public final class ForumController extends InferiorController {
    @RequestMapping(value = "/create", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ForumView> createForum(@RequestBody final ForumView forum) {
        try {
            jdbcForumDAO.create(forum.getUser(), forum.getSlug(), forum.getTitle());
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(jdbcForumDAO.findBySlug(forum.getSlug()));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(jdbcForumDAO.findBySlug(forum.getSlug()));
    }

    @RequestMapping(value = "/{slug}/create", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ThreadView> createSlug(@RequestBody ThreadView thread,
                                                       @PathVariable(value = "slug") final String slug) {
        final String threadSlug = thread.getSlug();
        try {
            thread = jdbcThreadDAO.create(thread.getAuthor(), thread.getCreated(), slug,
                    thread.getMessage(), thread.getSlug(), thread.getTitle());
            if (thread == null) {
                throw new EmptyResultDataAccessException(0);
            }
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(jdbcThreadDAO.findByIdOrSlug(threadSlug));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(thread);
    }

    @RequestMapping(value = "/{slug}/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ForumView> viewForum(@PathVariable("slug") final String slug) {
        final ForumView forum;
        try {
            forum = jdbcForumDAO.findBySlug(slug);
            if (forum == null) {
                throw new EmptyResultDataAccessException(0);
            }
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(forum);
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(value = "/{slug}/threads", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<List<ThreadView>> viewThreads(
            @RequestParam(value = "limit", required = false, defaultValue = "100") final Integer limit,
            @RequestParam(value = "since", required = false) final String since,
            @RequestParam(value = "desc", required = false, defaultValue = "false") final Boolean desc,
            @PathVariable("slug") final String slug) {
        try {
            final ForumView forum = jdbcForumDAO.findBySlug(slug);
            if (forum == null) {
                throw new EmptyResultDataAccessException(0);
            }
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(jdbcForumDAO.findAllThreads(slug, limit, since, desc));
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(value = "/{slug}/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<List<UserView>> viewUsers(
            @RequestParam(value = "limit", required = false, defaultValue = "100") final Integer limit,
            @RequestParam(value = "since", required = false) final String since,
            @RequestParam(value = "desc", required = false, defaultValue = "false") final Boolean desc,
            @PathVariable("slug") final String slug) {
        try {
            final ForumView forum = jdbcForumDAO.findBySlug(slug);
            if (forum == null) {
                throw new EmptyResultDataAccessException(0);
            }
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(jdbcForumDAO.findAllUsers(slug, limit, since, desc));
    }
}
