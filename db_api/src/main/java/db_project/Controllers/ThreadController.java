package db_project.Controllers;

import db_project.Views.*;
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
@RequestMapping(value = "/api/thread/{slug_or_id}")
public class ThreadController extends InferiorController {
    @RequestMapping(value = "/create", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createPosts(@RequestBody List<PostView> posts,
                                              @PathVariable(value = "slug_or_id") final String slug_or_id) {
        try {
            ThreadView thread = jdbcThreadDAO.findByIdOrSlug(slug_or_id);
            if (posts.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(posts);
            }
            for (PostView post : posts) {
                if (post.getParent() != 0) {
                    try {
                        PostView parent = jdbcPostDAO.findById(post.getParent());
                        post.setForum(thread.getForum());
                        post.setThread(thread.getId());
                        if (!thread.getId().equals(parent.getThread())) {
                            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                                    new ErrorView("{\"message\": \"Thread id is not equal to parent post thread id!\"}"));
                        }
                    } catch (EmptyResultDataAccessException ex) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorView(ex.getLocalizedMessage()));
                    }
                }
                post.setForum(thread.getForum());
                post.setThread(thread.getId());
            }
            jdbcPostDAO.create(posts, slug_or_id);
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorView(ex.getLocalizedMessage()));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorView(ex.getLocalizedMessage()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(posts);
    }

    @RequestMapping(value = "/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> viewThread(@PathVariable(value = "slug_or_id") final String slug_or_id) {
        final ThreadView thread;
        try {
            thread = jdbcThreadDAO.findByIdOrSlug(slug_or_id);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorView(ex.getLocalizedMessage()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }

    @RequestMapping(value = "/details", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updateThread(@RequestBody ThreadView thread,
                                               @PathVariable(value = "slug_or_id") final String slug_or_id) {
        try {
            jdbcThreadDAO.update(thread.getMessage(), thread.getTitle(), slug_or_id);
            thread = jdbcThreadDAO.findByIdOrSlug(slug_or_id);
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(jdbcThreadDAO.findByIdOrSlug(slug_or_id));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorView(ex.getLocalizedMessage()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }

    @RequestMapping(value = "/vote", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> voteForThread(@RequestBody final VoteView vote,
                                                @PathVariable("slug_or_id") final String slug_or_id) {
        final ThreadView thread;
        try {
            thread = jdbcThreadDAO.updateVotes(vote, slug_or_id);
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(jdbcThreadDAO.findByIdOrSlug(slug_or_id));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorView(ex.getLocalizedMessage()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }

    @RequestMapping(value = "/posts", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getPostsSorted(@PathVariable(value = "slug_or_id") final String slug_or_id,
                                         @RequestParam(value = "limit", required = false) final Integer limit,
                                         @RequestParam(value = "since", required = false) final Integer since,
                                         @RequestParam(value = "sort", required = false) final String sort,
                                         @RequestParam(value = "desc", required = false) final Boolean desc) {
        try {
            ThreadView thread = jdbcThreadDAO.findByIdOrSlug(slug_or_id);
            List<PostView> result = jdbcPostDAO.sort(thread, slug_or_id, limit, since, sort, desc);
            return ResponseEntity.status(HttpStatus.OK).body(result);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Error(ex.getMessage()));
        }
    }
}
