package db_project.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lieroz on 28.02.17.
 */
public class PostModel {
    private String author;
    private String created;
    private String forum;
    private Integer id;
    private Boolean isEdited;
    private String message;
    private Integer parent;
    private Integer thread;

    @JsonCreator
    public PostModel(
            @JsonProperty("author") final String author,
            @JsonProperty("created") final String created,
            @JsonProperty("forum") final String forum,
            @JsonProperty("id") final Integer id,
            @JsonProperty("isEdited") final Boolean isEdited,
            @JsonProperty("message") final String message,
            @JsonProperty("parent") final Integer parent,
            @JsonProperty("thread") final Integer thread
    ) {
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.id = id;
        this.isEdited = isEdited;
        this.message = message;
        this.parent = parent == null ? 0 : parent;
        this.thread = thread;
    }

    public PostModel(final PostModel other) {
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.id = id;
        this.isEdited = isEdited;
        this.message = message;
        this.parent = parent;
        this.thread = thread;
    }

    public final String getAuthor() {
        return this.author;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }

    public final String getCreated() {
        return this.created;
    }

    public void setCreated(final String created) {
        this.created = created;
    }

    public final String getForum() {
        return this.forum;
    }

    public void setForum(final String forum) {
        this.forum = forum;
    }

    public final Integer getId() {
        return this.id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public final Boolean getIsEdited() {
        return this.isEdited;
    }

    public void setIsEdited(final Boolean isEdited) {
        this.isEdited = isEdited;
    }

    public final String getMessage() {
        return this.message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public final Integer getParent() {
        return this.parent;
    }

    public void setParent(final Integer parent) {
        this.parent = parent;
    }

    public final Integer getThread() {
        return this.thread;
    }

    public void setThread(final Integer thread) {
        this.thread = thread;
    }
}
