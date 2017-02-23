package db_project.controllers;

import db_project.models.UserModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lieroz on 23.02.17.
 */

@RestController
@RequestMapping("/api")
public class UserController {
    private static Map<Integer, UserModel> dbModel = new HashMap<>();
    private JdbcTemplate jdbcTemplate;

    public UserController(JdbcTemplate template) {
        this.jdbcTemplate= template;
    }

    @RequestMapping(value = "/user/{nickname}/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserModel> createUser(
            @RequestBody UserModel userBody,
            @PathVariable(value = "nickname") String nickname
    ) {
//        dbModel.put(nickname.hashCode(), new UserModel(userBody.getAbout(), userBody.getEmail(), userBody.getFullname(), nickname));
        String SQL = "INSERT INTO Users (about, email, fullname, nickname) VALUES(?, ?, ?, ?);";
        jdbcTemplate.update(SQL, userBody.getAbout(), userBody.getEmail(), userBody.getFullname(), nickname);
        return new ResponseEntity<>(new UserModel(userBody.getAbout(), userBody.getEmail(), userBody.getFullname(), nickname), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/user/{nickname}/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserModel> viewProfile(
            @PathVariable(value = "nickname") String nickname
    ) {
        return new ResponseEntity<>(dbModel.get(nickname.hashCode()), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{nickname}/profile", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserModel> modifyProfile(
            @RequestBody UserModel userBody,
            @PathVariable(value = "nickname") String nickname
    ) {
        UserModel user = dbModel.get(nickname.hashCode());

        if (userBody.getAbout() != null) {
            user.setAbout(userBody.getAbout());
        }

        if (userBody.getEmail() != null) {
            user.setEmail(userBody.getEmail());
        }

        if (userBody.getFullname() != null) {
            user.setFullname(userBody.getFullname());
        }

        if (userBody.getNickname() != null) {
            user.setNickname(userBody.getNickname());
            dbModel.remove(nickname.hashCode());
            dbModel.put(userBody.getNickname().hashCode(), new UserModel(user));
        }

        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
