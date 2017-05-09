package db_project.Views;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lieroz on 9.05.17.
 */
public class VoteView {
    private String nickname;
    private Integer voice;

    public VoteView(@JsonProperty("nickname") final String nickname,
                    @JsonProperty("voice") final Integer voice) {
        this.nickname = nickname;
        this.voice = voice;
    }

    public final String getNickname() {
        return this.nickname;
    }

    public void setNickname(final String nickname) {
        this.nickname = nickname;
    }

    public final Integer getVoice() {
        return this.voice;
    }

    public void setVoice(final Integer voice) {
        this.voice = voice;
    }
}
