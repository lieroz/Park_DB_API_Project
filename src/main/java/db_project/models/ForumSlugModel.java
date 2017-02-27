package db_project.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lieroz on 27.02.17.
 */
public class ForumSlugModel {
    private String author;
    private String forum;
    private String slug;
    private String title;

    @JsonCreator
    public ForumSlugModel(
            @JsonProperty("author") final String author,
            @JsonProperty("forum") final String forum,
            @JsonProperty("slug") final String slug,
            @JsonProperty("title") final String title
    ) {
        this.author = author;
        this.forum = forum;
        this.slug = slug;
        this.title = title;
    }

    public ForumSlugModel(final ForumSlugModel other) {
        this.author = other.getAuthor();
        this.forum = other.getForum();
        this.slug = other.getSlug();
        this.title = other.getTitle();
    }

    public final String getAuthor() {
        return this.author;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }

    public final String getForum() {
        return this.forum;
    }

    public void setForum(final String forum) {
        this.forum = forum;
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
}
