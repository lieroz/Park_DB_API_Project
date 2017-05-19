package db_project.Controllers;

import db_project.Views.PostDetailedView;
import db_project.Views.PostView;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by lieroz on 4.03.17.
 */
@RestController
@RequestMapping("/api/post/{id}")
public class PostController extends InferiorController {
    @RequestMapping(value = "/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostDetailedView> viewForum(
            @RequestParam(value = "related", required = false) String[] related, @PathVariable("id") final Integer id) {
        final PostDetailedView post;
        try {
            post = jdbcPostDAO.detailedView(id, related);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(post);
    }

    @RequestMapping(value = "/details", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostView> viewForum(@RequestBody PostView post, @PathVariable("id") final Integer id) {
        try {
            post = post.getMessage() != null ? jdbcPostDAO.update(post.getMessage(), id) : jdbcPostDAO.findById(id);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(post);
    }
}
