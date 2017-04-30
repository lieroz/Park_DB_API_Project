package db_project.controllers;

import db_project.models.PostModel;
import db_project.models.PostsMarkerModel;
import db_project.models.ThreadModel;
import db_project.models.VoteModel;
import db_project.services.ThreadService;
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
@RequestMapping(value = "/api/thread/{slug_or_id}")
public class ThreadController {
    private final JdbcTemplate jdbcTemplate;
    private final ThreadService service;

    public ThreadController(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.service = new ThreadService(jdbcTemplate);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<List<PostModel>> createPosts(@RequestBody List<PostModel> posts,
            @PathVariable(value = "slug_or_id") final String slug_or_id) {
        try {

            if (posts.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

            service.createPosts(posts, slug_or_id);

        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);

        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(posts);
    }

    @RequestMapping(value = "/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ThreadModel> viewThread(@PathVariable(value = "slug_or_id") final String slug_or_id) {
        final ThreadModel thread;

        try {
            thread = service.getThreadInfo(slug_or_id);

            if (thread == null) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }

    @RequestMapping(value = "/details", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ThreadModel> updateThread(
            @RequestBody ThreadModel thread,
            @PathVariable(value = "slug_or_id") final String slug_or_id
    ) {
        try {
            service.updateThreadInfoFromDb(thread, slug_or_id);
            thread = service.getThreadInfo(slug_or_id);

            if (thread == null) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DuplicateKeyException ex) {
            return new ResponseEntity<>(service.getThreadInfo(slug_or_id), HttpStatus.CONFLICT);

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(thread, HttpStatus.OK);
    }

    @RequestMapping(value = "/vote", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ThreadModel> voteForThread(@RequestBody final VoteModel vote,
            @PathVariable("slug_or_id") final String slug_or_id) {
        final List<ThreadModel> threads;

        try {
            threads = service.updateVotes(vote, slug_or_id);

            if (threads.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DuplicateKeyException ex) {
            return new ResponseEntity<>(service.getThreadInfo(slug_or_id), HttpStatus.CONFLICT);

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(threads.get(0), HttpStatus.OK);
    }

    private static Integer offset = 0;

    @RequestMapping(value = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<PostsMarkerModel> viewThreads(
            @RequestParam(value = "limit", required = false, defaultValue = "100") final Integer limit,
            @RequestParam(value = "marker", required = false) final String marker,
            @RequestParam(value = "sort", required = false, defaultValue = "flat") final String sort,
            @RequestParam(value = "desc", required = false, defaultValue = "false") final Boolean desc,
            @PathVariable("slug_or_id") final String slug_or_id) {
        if (marker == null && offset != 0) {
            offset = 0;
        }

        final List<PostModel> posts = service.getSortedPosts(limit, offset, sort, desc, slug_or_id);
        offset += limit;

        if (posts.isEmpty() && marker == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new PostsMarkerModel(marker, posts), HttpStatus.OK);
    }
}
