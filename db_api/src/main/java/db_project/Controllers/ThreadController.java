package db_project.Controllers;

import db_project.Views.PostView;
import db_project.Views.PostsSortedView;
import db_project.Views.ThreadView;
import db_project.Views.VoteView;
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
    public ResponseEntity<List<PostView>> createPosts(@RequestBody List<PostView> posts,
                                                            @PathVariable(value = "slug_or_id") final String slug_or_id) {
        try {
            ThreadView thread = jdbcThreadDAO.findByIdOrSlug(slug_or_id);
            if (posts.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            for (PostView post : posts) {
                if (post.getParent() != 0) {
                    try {
                        PostView parent = jdbcPostDAO.findById(post.getParent());
                        post.setForum(thread.getForum());
                        post.setThread(thread.getId());
                        if (!thread.getId().equals(parent.getThread())) {
                            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
                        }
                    } catch (EmptyResultDataAccessException ex) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
                    }
                }
                post.setForum(thread.getForum());
                post.setThread(thread.getId());
            }
            jdbcPostDAO.create(posts, slug_or_id);
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(posts);
    }

    @RequestMapping(value = "/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ThreadView> viewThread(@PathVariable(value = "slug_or_id") final String slug_or_id) {
        final ThreadView thread;
        try {
            thread = jdbcThreadDAO.findByIdOrSlug(slug_or_id);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }

    @RequestMapping(value = "/details", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ThreadView> updateThread(@RequestBody ThreadView thread,
                                                         @PathVariable(value = "slug_or_id") final String slug_or_id) {
        try {
            jdbcThreadDAO.update(thread.getMessage(), thread.getTitle(), slug_or_id);
            thread = jdbcThreadDAO.findByIdOrSlug(slug_or_id);
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(jdbcThreadDAO.findByIdOrSlug(slug_or_id));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }

    @RequestMapping(value = "/vote", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ThreadView> voteForThread(@RequestBody final VoteView vote,
                                                          @PathVariable("slug_or_id") final String slug_or_id) {
        final ThreadView thread;
        try {
            thread = jdbcThreadDAO.updateVotes(vote, slug_or_id);
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(jdbcThreadDAO.findByIdOrSlug(slug_or_id));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }

    @RequestMapping(value = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostsSortedView> viewThreads(
            @RequestParam(value = "limit", required = false, defaultValue = "100") final Integer limit,
            @RequestParam(value = "marker", required = false) String marker,
            @RequestParam(value = "sort", required = false, defaultValue = "flat") final String sort,
            @RequestParam(value = "desc", required = false, defaultValue = "false") final Boolean desc,
            @PathVariable("slug_or_id") final String slug_or_id) {
        if (marker == null) {
            marker = "0";
        }
        final List<PostView> posts = jdbcPostDAO.sort(limit, Integer.parseInt(marker), sort, desc, slug_or_id);
        if (posts.isEmpty() && marker.equals("0")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new PostsSortedView(
                !posts.isEmpty() ? String.valueOf(Integer.parseInt(marker) + limit) : marker, posts));
    }
}
