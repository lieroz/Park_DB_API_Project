package db_project.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lieroz on 27.02.17.
 */

public class ForumModel {
    private String slug;
    private String title;
    private String user;

    @JsonCreator
    public ForumModel(
            @JsonProperty("slug") final String slug,
            @JsonProperty("title") final String title,
            @JsonProperty("user") final String user
    ) {
        this.slug = slug;
        this.title = title;
        this.user = user;
    }

    public ForumModel(final ForumModel other) {
        this.slug = other.getSlug();
        this.title = other.getTitle();
        this.user = other.getUser();
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

    public final String getUser() {
        return this.user;
    }

    public void setUser(final String user) {
        this.user = user;
    }
}
