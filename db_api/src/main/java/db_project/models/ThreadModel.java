package db_project.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lieroz on 27.02.17.
 */

/**
 * @brief Model representing Thread in database.
 */

public class ThreadModel {
    /**
     * @brief Fields which represent info about thread.
     */

    private String author;
    private String created;
    private String forum;
    private Integer id;
    private String message;
    private String slug;
    private String title;
    private Integer votes;

    /**
     * @brief Constructor that serializes object into JSON.
     */

    @JsonCreator
    public ThreadModel(
            @JsonProperty("author") final String author,
            @JsonProperty("created") final String created,
            @JsonProperty("forum") final String forum,
            @JsonProperty("id") final Integer id,
            @JsonProperty("message") final String message,
            @JsonProperty("slug") final String slug,
            @JsonProperty("title") final String title,
            @JsonProperty("votes") final Integer votes
    ) {
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.id = id;
        this.message = message;
        this.slug = slug;
        this.title = title;
        this.votes = votes;
    }

    /**
     * @brief Copy Constructor.
     */

    public ThreadModel(final ThreadModel other) {
        this.author = other.getAuthor();
        this.created = other.getCreated();
        this.forum = other.getForum();
        this.id = other.getId();
        this.message = other.getMessage();
        this.slug = other.getSlug();
        this.title = other.getTitle();
        this.votes = other.getVotes();
    }

    /**
     * @brief Getters and setters.
     * @brief Getters are need for object serialization into JSON.
     */

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

    public final String getMessage() {
        return this.message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public final String getSlug() {
        return this.slug;
    }

    public void setSlug(final String slug) {
        this.slug = slug;
    }

    public final String getTitle() {
        return this.title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public final Integer getVotes() {
        return this.votes;
    }

    public void setVotes(final Integer votes) {
        this.votes = votes;
    }
}
