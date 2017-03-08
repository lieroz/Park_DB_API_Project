package db_project.controllers;

import db_project.models.UserModel;
import db_project.services.UserService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by lieroz on 23.02.17.
 */

/**
 * @brief Implementation of class that is responsible for handling all requests about user.
 */

@RestController
@RequestMapping(value = "/api/user/{nickname}")
public final class UserController {
    /**
     * @brief Class used for communication with database.
     */
    private final JdbcTemplate jdbcTemplate;
    /**
     * @brief Wrapper on JdbcTemplate for more convenient usage.
     */
    private final UserService service;

    public UserController(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.service = new UserService(jdbcTemplate);
    }

    /**
     * @brief User creation method.
     */

    @RequestMapping(value = "/create",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<Object> createUser(
            @RequestBody UserModel user,
            @PathVariable(value = "nickname") String nickname
    ) {
        user.setNickname(nickname);

        try {
            service.insertUserIntoDb(user);

        } catch (DuplicateKeyException ex) {
            return new ResponseEntity<>(service.getUserFromDb(user), HttpStatus.CONFLICT);

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new UserModel(user), HttpStatus.CREATED);
    }

    /**
     * @brief Get user profile method.
     */

    @RequestMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<UserModel> viewProfile(
            @PathVariable(value = "nickname") String nickname
    ) {
        final List<UserModel> users;

        try {
            users = service.getUserFromDb(new UserModel(null, null, null, nickname));

            if (users.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(users.get(0), HttpStatus.OK);
    }

    /**
     * @brief Change user profile method.
     */

    @RequestMapping(value = "/profile",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<UserModel> modifyProfile(
            @RequestBody UserModel user,
            @PathVariable(value = "nickname") String nickname
    ) {
        user.setNickname(nickname);

        try {
            service.updateUserInfoFromDb(user);
            final List<UserModel> users = service.getUserFromDb(user);

            if (users.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

            user = users.get(0);

        } catch (DuplicateKeyException ex) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);

        } catch (DataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new UserModel(user), HttpStatus.OK);
    }
}
