package db_project.controllers;

import db_project.models.*;
import db_project.services.PostService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by lieroz on 4.03.17.
 */
@RestController
@RequestMapping("/api/post/{id}")
public class PostController {
    private final JdbcTemplate jdbcTemplate;
    private final PostService service;

    public PostController(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.service = new PostService(jdbcTemplate);
    }

    @RequestMapping(value = "/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<PostDetailsModel> viewForum(
            @RequestParam(value = "related", required = false) String[] related, @PathVariable("id") final Integer id) {
        final PostDetailsModel post;

        try {
            post = service.getDetailedPostInfo(id, related);

        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.status(HttpStatus.OK).body(post);
    }

    @RequestMapping(value = "/details", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<PostModel> viewForum(@RequestBody PostModel post, @PathVariable("id") final Integer id) {
        try {

            if (post.getMessage() != null) {
                post = service.updatePost(post.getMessage(), id);

            } else {
                post = service.getPost(id);
            }

        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.status(HttpStatus.OK).body(post);
    }
}
