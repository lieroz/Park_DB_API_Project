package db_project.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lieroz on 23.02.17.
 */

/**
 * @ brief Model representing User in database.
 */

public class UserModel {
    /**
     * @ brief Fields which represent info about user.
     */

    private String about;
    private String email;
    private String fullname;
    private String nickname;

    /**
     * @ brief Constructor that serializes object into JSON.
     */

    @JsonCreator
    public UserModel(
            @JsonProperty("about") final String about,
            @JsonProperty("email") final String email,
            @JsonProperty("fullname") final String fullname,
            @JsonProperty("nickname") final String nickname
    ) {
        this.about = about;
        this.email = email;
        this.fullname = fullname;
        this.nickname = nickname;
    }

    /**
     * @ brief Copy Constructor.
     */

    public UserModel(final UserModel other) {
        this.about = other.getAbout();
        this.email = other.getEmail();
        this.fullname = other.getFullname();
        this.nickname = other.getNickname();
    }

    /**
     * @ brief Getters and setters.
     * @ brief Getters are need for object serialization into JSON.
     */

    public final String getAbout() {
        return this.about;
    }

    public void setAbout(final String about) {
        this.about = about;
    }

    public final String getEmail() {
        return this.email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public final String getFullname() {
        return this.fullname;
    }

    public void setFullname(final String fullname) {
        this.fullname = fullname;
    }

    public final String getNickname() {
        return this.nickname;
    }

    public void setNickname(final String nickname) {
        this.nickname = nickname;
    }
}
