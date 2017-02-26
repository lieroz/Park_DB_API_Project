package db_project.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lieroz on 27.02.17.
 */

public class ThreadModel {
    private String author;
    private String created;
    private String forum;
    private Integer id;
    private Boolean edited;
    private String message;
    private Integer parent;
    private Integer thread;
    private String slug;
    private String Title;
    private Integer votes;

    @JsonCreator
    public ThreadModel(
            @JsonProperty("author") String author,
            @JsonProperty("created") String created,
            @JsonProperty("forum") String forum,
            @JsonProperty("id") Integer id,
            @JsonProperty("edited") Boolean edited,
            @JsonProperty("message") String message,

            ) {

    }
}
