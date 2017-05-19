package db_project.Controllers;

import db_project.Views.UserView;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by lieroz on 23.02.17.
 */

@RestController
@RequestMapping(value = "/api/user/{nickname}")
public class UserController extends InferiorController {
    @RequestMapping(value = "/create", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createUser(@RequestBody UserView user,
                                                   @PathVariable(value = "nickname") String nickname) {
        try {
            jdbcUserDAO.create(user.getAbout(), user.getEmail(), user.getFullname(), nickname);
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(jdbcUserDAO.findManyByNickOrMail(nickname, user.getEmail()));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        user.setNickname(nickname);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @RequestMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserView> viewProfile(@PathVariable(value = "nickname") String nickname) {
        final UserView user;
        try {
            user = jdbcUserDAO.findSingleByNickOrMail(nickname, null);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @RequestMapping(value = "/profile", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserView> modifyProfile(@RequestBody UserView user,
                                                        @PathVariable(value = "nickname") String nickname) {
        try {
            jdbcUserDAO.update(user.getAbout(), user.getEmail(), user.getFullname(), nickname);
            user = jdbcUserDAO.findSingleByNickOrMail(nickname, user.getEmail());
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
}
