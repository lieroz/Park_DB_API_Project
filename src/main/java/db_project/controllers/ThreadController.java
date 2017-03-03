package db_project.controllers;

import db_project.models.ForumModel;
import db_project.models.PostModel;
import db_project.models.ThreadModel;
import db_project.services.ThreadService;
import org.springframework.dao.DataAccessException;
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
 * @ Implementation of class that is responsible for handling all requests about thread.
 */

@RestController
@RequestMapping(value = "/api/thread")
public class ThreadController {
    /**
     * @ brief Class used for communication with database.
     */
    private final JdbcTemplate jdbcTemplate;
    /**
     * @ brief Wrapper on JdbcTemplate for more convenient usage.
     */
    private final ThreadService service;

    public ThreadController(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.service = new ThreadService(jdbcTemplate);
    }

    // TODO Where goes id here???

//    @RequestMapping(value = "/{id}/create",
//            method = RequestMethod.POST,
//            produces = MediaType.APPLICATION_JSON_VALUE,
//            consumes = MediaType.APPLICATION_JSON_VALUE)
//    public final ResponseEntity<List<PostModel>> createPosts(
//            @RequestBody List<PostModel> posts,
//            @PathVariable(value = "id") final Integer id
//    ) {
//        List<ThreadModel> test = jdbcTemplate.query(
//                "SELECT * FROM threads WHERE id = ?",
//                new Object[]{id + 1},
//                ThreadService::read
//        );
//
//        for (PostModel post : posts) {
//            post.setThread(id + 1);
//            post.setForum(test.get(0).getForum());
//        }
//
//        return new ResponseEntity<>(posts, HttpStatus.CREATED);
//    }

    /**
     * @ brief Get details about spesific thread.
     * @ brief {slug} stands for thread-slug.
     */

    @RequestMapping(value = "{slug}/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<ThreadModel> viewThread(
            @PathVariable(value = "slug") final String slug
    ) {
        List<ThreadModel> threads;

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
}
