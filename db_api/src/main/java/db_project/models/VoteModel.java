package db_project.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lieroz on 3.03.17.
 */

public class VoteModel {
    private String nickname;
    private Integer voice;

    @JsonCreator
    public VoteModel(
            @JsonProperty("nickname") final String nickname,
            @JsonProperty("voice") final Integer voice
    ) {
        this.nickname = nickname;
        this.voice = voice;
    }

    public VoteModel(final VoteModel other) {
        this.nickname = other.getNickname();
        this.voice = other.getVoice();
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
