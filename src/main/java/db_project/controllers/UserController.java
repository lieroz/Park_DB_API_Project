package db_project.controllers;

import db_project.models.UserModel;
import db_project.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by lieroz on 23.02.17.
 */

@RestController
@RequestMapping("/api")
public class UserController {
    private final JdbcTemplate jdbcTemplate;
    private final UserService service;

    public UserController(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        service = new UserService(jdbcTemplate);
    }

    @RequestMapping(value = "/user/{nickname}/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserModel> createUser(
            @RequestBody UserModel user,
            @PathVariable(value = "nickname") String nickname
    ) {
        user.setNickname(nickname);
        final Integer rowsAffected = service.insertUserIntoDb(user);

        if (rowsAffected == 0) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(new UserModel(user), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/user/{nickname}/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserModel> viewProfile(
            @PathVariable(value = "nickname") String nickname
    ) {
        List<UserModel> users = service.getUserFromDb(nickname);

        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(users.get(0), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{nickname}/profile", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserModel> modifyProfile(
            @RequestBody UserModel user,
            @PathVariable(value = "nickname") String nickname
    ) {
        user.setNickname(nickname);
        user = service.updateUserInfoFromDb(user);

        return new ResponseEntity<>(new UserModel(user), HttpStatus.OK);
    }
}
