package db_project.controllers;

import db_project.models.UserModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by lieroz on 23.02.17.
 */

@RestController
@RequestMapping("/api")
public class UserController {
    private final JdbcTemplate jdbcTemplate;

    public UserController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @RequestMapping(value = "/user/{nickname}/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserModel> createUser(
            @RequestBody UserModel user,
            @PathVariable(value = "nickname") String nickname
    ) {
        String sql = "INSERT INTO Users (about, email, fullname, nickname) VALUES(?, ?, ?, ?);";
        jdbcTemplate.update(sql, user.getAbout(), user.getEmail(), user.getFullname(), nickname);
        return new ResponseEntity<>(new UserModel(user.getAbout(), user.getEmail(), user.getFullname(), nickname), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/user/{nickname}/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserModel> viewProfile(
            @PathVariable(value = "nickname") String nickname
    ) {
        String sql = "SELECT * FROM Users WHERE nickname = ?";
        List<UserModel> users = jdbcTemplate.query(sql, new Object[]{nickname}, UserController::readItem);
        return new ResponseEntity<>(users.get(0), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{nickname}/profile", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserModel> modifyProfile(
            @RequestBody UserModel user,
            @PathVariable(value = "nickname") String nickname
    ) {
        String sql = "UPDATE Users SET about = ?, email = ?, fullname = ? WHERE nickname = ?;";
        jdbcTemplate.update(sql, user.getAbout(), user.getEmail(), user.getFullname(), nickname);
        return new ResponseEntity<>(new UserModel(user.getAbout(), user.getEmail(), user.getFullname(), nickname), HttpStatus.OK);
    }

    private static UserModel readItem(ResultSet rs, int rowNum) throws SQLException {
        return new UserModel(rs.getString("about"), rs.getString("email"), rs.getString("fullname"), rs.getString("nickname"));
    }
}
