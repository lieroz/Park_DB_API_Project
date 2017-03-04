package db_project.controllers;

/**
 * Created by lieroz on 4.03.17.
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import db_project.models.ForumModel;
import db_project.models.PostModel;
import db_project.models.ThreadModel;
import db_project.models.UserModel;
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
    public final ResponseEntity<PostDetails> viewForum(
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

        UserModel user = null;
        ForumModel forum = null;
        ThreadModel thread = null;

        if (related != null) {

            for (String relation : related) {

                if (Objects.equals(relation, "user")) {
                    UserService userService = new UserService(jdbcTemplate);
                    List<UserModel> users = userService.getUserFromDb(new UserModel(null, null, null, posts.get(0).getAuthor()));

                    if (!users.isEmpty()) {
                        user = users.get(0);
                    }
                }

                if (relation.equals("forum")) {
                    ForumService forumService = new ForumService(jdbcTemplate);
                    List<ForumModel> forums = forumService.getForumInfo(posts.get(0).getForum());

                    if (!forums.isEmpty()) {
                        forum = forums.get(0);
                    }

                    forum.setThreads(jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM threads WHERE LOWER(forum) = LOWER(?)",
                            Integer.class,
                            forum.getSlug()
                    ));
                }

                if (relation.equals("thread")) {
                    ThreadService forumService = new ThreadService(jdbcTemplate);
                    List<ThreadModel> threads = forumService.getThreadInfoById(posts.get(0).getThread());

                    if (!threads.isEmpty()) {
                        thread = threads.get(0);
                    }
                }
            }
        }

        return new ResponseEntity<>(new PostDetails(user, forum, posts.get(0), thread), HttpStatus.OK);
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

    public class PostDetails {
        private UserModel author;
        private ForumModel forum;
        private PostModel post;
        private ThreadModel thread;

        @JsonCreator
        public PostDetails(
                @JsonProperty("author") final UserModel author,
                @JsonProperty("forum") final ForumModel forum,
                @JsonProperty("post") final PostModel post,
                @JsonProperty("thread") final ThreadModel thread
        ) {
            this.author = author;
            this.forum = forum;
            this.post = post;
            this.thread = thread;
        }

        public final UserModel getAuthor() {
            return this.author;
        }

        public void setAuthor(final UserModel author) {
            this.author = author;
        }

        public final ForumModel getForum() {
            return this.forum;
        }

        public void setForum(ForumModel forum) {
            this.forum = forum;
        }

        public final PostModel getPost() {
            return this.post;
        }

        public void setPost(PostModel post) {
            this.post = post;
        }

        public final ThreadModel getThread() {
            return this.thread;
        }

        public void setThread(ThreadModel thread) {
            this.thread = thread;
        }
    }
}
