package db_project.Views;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lieroz on 9.05.17.
 */
public class UserView {
    private String about;
    private String email;
    private String fullname;
    private String nickname;

    public UserView(@JsonProperty("about") final String about,
                    @JsonProperty("email") final String email,
                    @JsonProperty("fullname") final String fullname,
                    @JsonProperty("nickname") final String nickname) {
        this.about = about;
        this.email = email;
        this.fullname = fullname;
        this.nickname = nickname;
    }

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
