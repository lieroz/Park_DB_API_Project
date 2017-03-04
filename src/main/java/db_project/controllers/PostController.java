package db_project.controllers;

/**
 * Created by lieroz on 4.03.17.
 */

import db_project.models.*;
import db_project.services.ForumService;
import db_project.services.PostService;
import db_project.services.ThreadService;
import db_project.services.UserService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * @brief Implementation of class that is responsible for handling all requests about posts.
 */

@RestController
@RequestMapping("/api/post/{id}")
public class PostController {
    /**
     * @brief Class used for communication with database.
     */
    private final JdbcTemplate jdbcTemplate;
    /**
     * @brief Wrapper on JdbcTemplate for more convenient usage.
     */
    private final PostService service;

    public PostController(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.service = new PostService(jdbcTemplate);
    }

    /**
     * @brief Get details about specific post.
     * @brief {id} stands for post-id.
     */

    @RequestMapping(value = "/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<PostDetailsModel> viewForum(
            @RequestParam(value = "related", required = false) String[] related,
            @PathVariable("id") final Integer id
    ) {
        List<PostModel> posts;

        try {
            posts = service.getPostFromDb(id);


            if (posts.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(service.getDetailedPostFromDb(posts.get(0), related), HttpStatus.OK);
    }

    /**
     * @brief Update details about specific post.
     * @brief {id} stands for post-id.
     */

    @RequestMapping(value = "/details",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<PostModel> viewForum(
            @RequestBody final PostModel post,
            @PathVariable("id") final Integer id
    ) {
        List<PostModel> posts;

        try {
            if (post.getMessage() != null && !post.getMessage().isEmpty()) {
                posts = service.updatePostInDb(post, id);

            } else {
                posts = service.getPostFromDb(id);
            }

            if (posts.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(posts.get(0), HttpStatus.OK);
    }
}
