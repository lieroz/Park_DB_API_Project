package db_project.controllers;

import db_project.models.UserModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * Created by lieroz on 23.02.17.
 */

@RestController
@RequestMapping("/api")
public class UserController {

    @RequestMapping(value = "/user", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public UserModel createUser(@RequestBody UserModel userModel, HttpSession httpSession) {
        httpSession.setAttribute("about", userModel.getAbout());
        httpSession.setAttribute("email", userModel.getEmail());
        httpSession.setAttribute("fullname", userModel.getFullname());
        httpSession.setAttribute("nickname", userModel.getNickname());

        return userModel;
    }

    @RequestMapping(value = "/user/{nickname}/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserModel viewProfile(@RequestParam(value = "about", defaultValue = "DEFAULT ABOUT") String about,
                                 @RequestParam(value = "email", defaultValue = "DEFAULT EMAIL") String email,
                                 @RequestParam(value = "fullname", defaultValue = "DEFAULT FULLNAME") String fullname,
                                 @PathVariable(value = "nickname") String nickname) {
        return new UserModel(about, email, fullname, nickname);
    }
}
