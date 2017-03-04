package db_project.controllers;

import db_project.models.PostModel;
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

/**
 * @brief Implementation of class that is responsible for handling all requests about thread.
 */

@RestController
@RequestMapping(value = "/api/thread/{slug}")
public class ThreadController {
    /**
     * @brief Class used for communication with database.
     */
    private final JdbcTemplate jdbcTemplate;
    /**
     * @brief Wrapper on JdbcTemplate for more convenient usage.
     */
    private final ThreadService service;

    public ThreadController(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.service = new ThreadService(jdbcTemplate);
    }

    @RequestMapping(value = "/create",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<List<PostModel>> createPosts(
            @RequestBody final List<PostModel> posts,
            @PathVariable(value = "slug") final String slug
    ) {
        final Integer id;
        final List<PostModel> postModelList;

        try {
            id = Integer.valueOf(slug);
            postModelList = service.insertPostsIntoDbById(posts, id);

        } catch (NumberFormatException ex) {
            final List<PostModel> dbPosts;

            try {
                dbPosts = service.insertPostsIntoDbBySlug(posts, slug);

            } catch (DuplicateKeyException e) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);

            } catch (DataAccessException e) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(dbPosts, HttpStatus.CREATED);

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(postModelList, HttpStatus.CREATED);
    }

    /**
     * @brief Get details about specific thread.
     * @brief {slug} stands for thread-slug.
     */

    @RequestMapping(value = "/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ThreadModel> viewThread(
            @PathVariable(value = "slug") final String slug
    ) {
        final List<ThreadModel> threads;

        try {
            threads = service.getThreadInfo(slug);

            if (threads.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(threads.get(0), HttpStatus.OK);
    }

    /**
     * @brief Update a specific thread.
     * @brief {slug} stands for thread-slug.
     */

    @RequestMapping(value = "/details",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ThreadModel> updateThread(
            @RequestBody final ThreadModel thread,
            @PathVariable(value = "slug") final String slug
    ) {
        final List<ThreadModel> threads;

        try {
            service.updateThreadInfoFromDb(thread, slug);
            threads = service.getThreadInfo(slug);

            if (threads.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DuplicateKeyException ex) {
            return new ResponseEntity<>(service.getThreadInfo(slug).get(0), HttpStatus.CONFLICT);

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(threads.get(0) ,HttpStatus.OK);
    }

    /**
     * @brief Vote for a specific thread.
     * @brief {slug} stands for thread-slug.
     */

    @RequestMapping(value = "/vote",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ThreadModel> voteForThread(
            @RequestBody final VoteModel vote,
            @PathVariable("slug") final String slug
    ) {
        final List<ThreadModel> threads;

        try {
            threads = service.updateVotes(vote, slug);

            if (threads.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DuplicateKeyException ex) {
            return new ResponseEntity<>(service.getThreadInfo(slug).get(0) ,HttpStatus.CONFLICT);

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(threads.get(0), HttpStatus.OK);
    }

    @RequestMapping(value = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<List<PostModel>> viewThreads(
            @RequestParam(value = "limit", required = false, defaultValue = "100") final Integer limit,
            @RequestParam(value = "marker", required = false) final String marker,
            @RequestParam(value = "sort", required = false, defaultValue = "flat") final String sort,
            @RequestParam(value = "desc", required = false) final Boolean desc,
            @PathVariable("slug") final String slug
    ) {
        /**
         * Here goe code with sorting posts
         * It will be done later =)
         * Test Nr 32
         */

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
