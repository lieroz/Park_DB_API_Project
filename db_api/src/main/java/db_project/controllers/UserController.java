package db_project.controllers;

import db_project.models.UserViewModel;
import db_project.services.UserService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody UserViewModel user,
            @PathVariable(value = "nickname") String nickname
    ) {
        try {
            service.createUser(user.getAbout(), user.getEmail(), user.getFullname(), nickname);

        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(service.getUsers(nickname, user.getEmail()));

        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        user.setNickname(nickname);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * @brief Get user profile method.
     */

    @RequestMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<UserViewModel> viewProfile(
            @PathVariable(value = "nickname") String nickname
    ) {
        final UserViewModel user;

        try {
            user = service.getUser(nickname, null);

            if (user == null) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    /**
     * @brief Change user profile method.
     */

    @RequestMapping(value = "/profile",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public final ResponseEntity<UserViewModel> modifyProfile(
            @RequestBody UserViewModel user,
            @PathVariable(value = "nickname") String nickname
    ) {
        try {
            service.updateUser(user.getAbout(), user.getEmail(), user.getFullname(), nickname);
            user = service.getUser(nickname, user.getEmail());

            if (user == null) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);

        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
}
