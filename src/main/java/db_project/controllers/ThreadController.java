package db_project.controllers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import db_project.models.PostModel;
import db_project.models.ThreadModel;
import db_project.models.VoteModel;
import db_project.services.PostService;
import db_project.services.ThreadService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        final List<PostModel> dbPosts;

        try {

            if (posts.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

            dbPosts = service.insertPostsIntoDb(posts, slug);

        } catch (DuplicateKeyException ex) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(dbPosts, HttpStatus.CREATED);
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

        return new ResponseEntity<>(threads.get(0), HttpStatus.OK);
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
            return new ResponseEntity<>(service.getThreadInfo(slug).get(0), HttpStatus.CONFLICT);

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(threads.get(0), HttpStatus.OK);
    }

    private static Integer markerValue = 0;

    @RequestMapping(value = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<Test> viewThreads(
            @RequestParam(value = "limit", required = false, defaultValue = "100") final Integer limit,
            @RequestParam(value = "marker", required = false) final String marker,
            @RequestParam(value = "sort", required = false, defaultValue = "flat") final String sort,
            @RequestParam(value = "desc", required = false) final Boolean desc,
            @PathVariable("slug") final String slug
    ) {
        if (marker != null && !Objects.equals(sort, "parent_tree")) {
            markerValue += limit;
        }

        final StringBuilder sql = new StringBuilder();
        Integer id = null;
        Boolean isNumber = false;

        try {
            id = Integer.valueOf(slug);
            isNumber = Boolean.TRUE;

            if (Objects.equals(sort, "flat")) {
                sql.append("SELECT * FROM posts WHERE posts.thread = " +
                        "(SELECT threads.id FROM threads WHERE threads.id = ?) ORDER BY posts.created");

            } else if (Objects.equals(sort, "tree")) {
                sql.append("WITH RECURSIVE some_threads AS (SELECT * FROM posts WHERE posts.thread = " +
                        "(SELECT threads.id FROM threads WHERE threads.id = ?)), " +
                        "tree AS (SELECT *, array[id] AS path " +
                        "FROM some_threads WHERE parent = 0 " +
                        "UNION " +
                        "SELECT " +
                        "st.*, tree.path || st.id AS path " +
                        "FROM tree " +
                        "JOIN some_threads st ON st.parent = tree.id) " +
                        "SELECT * FROM tree ORDER BY path");

            } else if (Objects.equals(sort, "parent_tree")) {
                sql.append("WITH RECURSIVE some_threads AS (SELECT * FROM posts WHERE posts.thread = " +
                        "(SELECT threads.id FROM threads WHERE threads.id = ?)), " +
                        "tree AS (SELECT *, array[id] AS path " +
                        "FROM some_threads WHERE parent = 0 " +
                        "UNION " +
                        "SELECT " +
                        "st.*, tree.path || st.id AS path " +
                        "FROM tree " +
                        "JOIN some_threads st ON st.parent = tree.id) " +
                        "SELECT * FROM tree ORDER BY path");
            }

        } catch (NumberFormatException ex) {
            if (Objects.equals(sort, "flat")) {
                sql.append("SELECT * FROM posts WHERE posts.thread = " +
                        "(SELECT threads.id FROM threads WHERE LOWER(threads.slug) = LOWER(?)) ORDER BY posts.created");

            } else if (Objects.equals(sort, "tree")) {
                sql.append("WITH RECURSIVE some_threads AS (SELECT * FROM posts WHERE posts.thread = " +
                        "(SELECT threads.id FROM threads WHERE LOWER(threads.slug) = LOWER(?))), " +
                        "tree AS (SELECT *, array[id] AS path " +
                        "FROM some_threads WHERE parent = 0 " +
                        "UNION " +
                        "SELECT " +
                        "st.*, tree.path || st.id AS path " +
                        "FROM tree " +
                        "JOIN some_threads st ON st.parent = tree.id) " +
                        "SELECT * FROM tree ORDER BY path");

            } else if (Objects.equals(sort, "parent_tree")) {
                sql.append("WITH RECURSIVE some_threads AS (SELECT * FROM posts WHERE posts.thread = " +
                        "(SELECT threads.id FROM threads WHERE LOWER(threads.slug) = LOWER(?))), " +
                        "tree AS (SELECT *, array[id] AS path " +
                        "FROM some_threads WHERE parent = 0 " +
                        "UNION " +
                        "SELECT " +
                        "st.*, tree.path || st.id AS path " +
                        "FROM tree " +
                        "JOIN some_threads st ON st.parent = tree.id) " +
                        "SELECT * FROM tree ORDER BY path");
            }
        }

        if (desc == Boolean.TRUE) {
            sql.append(" DESC");
        }

        final List<PostModel> posts = jdbcTemplate.query(
                sql.toString(),
                isNumber ? new Object[]{id} : new Object[]{slug},
                PostService::read
        );

        if (posts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Integer temp = 0;

        if (Objects.equals(sort, "parent_tree")) {
            Integer zeroCount = 0;

            if (markerValue >= posts.size() && marker != null) {
                markerValue = 0;
                return new ResponseEntity<>(new Test("some marker", new ArrayList<>()), HttpStatus.OK);
            }

            if (markerValue == posts.size()) {
                markerValue = 0;
            }

            for (PostModel post : posts.subList(markerValue, posts.size())) {

                if (zeroCount.equals(limit) && desc == Boolean.TRUE) {
                    break;
                }

                if (zeroCount.equals(limit + 1) && (desc == Boolean.FALSE || desc == null)) {
                    --temp;
                    break;
                }

                if (post.getParent().equals(0)) {
                    ++zeroCount;
                }

                ++temp;
            }

            final List<PostModel> lst = posts.subList(markerValue, markerValue + temp);
            markerValue += temp;

            return new ResponseEntity<>(new Test("some marker", lst), HttpStatus.OK);

        } else {
            temp = limit + markerValue > posts.size() ? posts.size() : limit + markerValue;
        }

        if (markerValue > posts.size()) {
            markerValue = 0;
            return new ResponseEntity<>(new Test("some marker", new ArrayList<>()), HttpStatus.OK);
        }

        return new ResponseEntity<>(new Test("some marker", posts.subList(markerValue, temp)), HttpStatus.OK);
    }

    public class Test {
        private List<PostModel> posts;
        private String marker;

        @JsonCreator
        public Test(
                @JsonProperty("marker") final String marker,
                @JsonProperty("posts") final List<PostModel> posts
        ) {
            this.marker = marker;
            this.posts = posts;
        }

        public final List<PostModel> getPosts() {
            return this.posts;
        }

        public void setPosts(final List<PostModel> posts) {
            this.posts = posts;
        }

        public final String getMarker() {
            return this.marker;
        }

        public void setMarker(final String marker) {
            this.marker = marker;
        }
    }
}
