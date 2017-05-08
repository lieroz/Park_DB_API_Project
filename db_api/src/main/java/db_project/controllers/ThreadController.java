package db_project.controllers;

import db_project.models.*;
import db_project.services.PostService;
import db_project.services.ThreadService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
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
    private final PostService postSerice;

    public ThreadController(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.service = new ThreadService(jdbcTemplate);
        this.postSerice = new PostService(jdbcTemplate);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<List<PostModel>> createPosts(@RequestBody List<PostModel> posts,
                                                             @PathVariable(value = "slug_or_id") final String slug_or_id) {
        List<PostModel> newPosts;

        try {
            ThreadModel thread = service.getThreadInfo(slug_or_id);

            if (posts.isEmpty() || thread == null) {
                throw new EmptyResultDataAccessException(0);
            }

            for (PostModel post : posts) {
                if (post.getParent() != 0) {

                    try {
                        PostModel parent = postSerice.getPost(post.getParent());
                        post.setForum(thread.getForum());
                        post.setThread(thread.getId());

                        if (!thread.getId().equals(parent.getThread())) {
                            throw new DuplicateKeyException(null);
                        }
                    } catch (DataAccessException ex) {
                        throw new DuplicateKeyException(null);
                    }
                }

                post.setForum(thread.getForum());
                post.setThread(thread.getId());
            }

            newPosts = service.createPosts(posts, slug_or_id);
        } catch (EmptyResultDataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);

        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(newPosts);
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
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
            service.updateThreadInfoFromDb(thread.getMessage(), thread.getTitle(), slug_or_id);
            thread = service.getThreadInfo(slug_or_id);

            if (thread == null) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(service.getThreadInfo(slug_or_id));

        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }

    @RequestMapping(value = "/vote", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ThreadModel> voteForThread(@RequestBody final VoteModel vote,
                                                           @PathVariable("slug_or_id") final String slug_or_id) {
        final ThreadModel thread;

        try {
            thread = service.updateThreadVotes(vote, slug_or_id);

            if (thread == null) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(service.getThreadInfo(slug_or_id));

        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.status(HttpStatus.OK).body(thread);
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.status(HttpStatus.OK).body(new PostsMarkerModel(marker, posts));
    }
}
