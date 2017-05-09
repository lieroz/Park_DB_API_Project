package db_project.Views;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lieroz on 9.05.17.
 */
public class ForumView {
    private Integer posts;
    private String slug;
    private Integer threads;
    private String title;
    private String user;

    public ForumView(@JsonProperty("posts") final Integer posts,
                     @JsonProperty("slug") final String slug,
                     @JsonProperty("threads") final Integer threads,
                     @JsonProperty("title") final String title,
                     @JsonProperty("user") final String user) {
        this.posts = posts;
        this.slug = slug;
        this.threads = threads;
        this.title = title;
        this.user = user;
    }

    public final Integer getPosts() {
        return this.posts;
    }

    public void setPosts(final Integer posts) {
        this.posts = posts;
    }

    public final String getSlug() {
        return this.slug;
    }

    public void setSlug(final String slug) {
        this.slug = slug;
    }

    public final Integer getThreads() {
        return this.threads;
    }

    public void setThreads(final Integer threads) {
        this.threads = threads;
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