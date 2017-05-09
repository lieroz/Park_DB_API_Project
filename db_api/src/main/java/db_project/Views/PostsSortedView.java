package db_project.Views;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by lieroz on 9.05.17.
 */
public class PostsSortedView {
    private List<PostView> posts;
    private String marker;

    public PostsSortedView(@JsonProperty("marker") final String marker,
                           @JsonProperty("posts") final List<PostView> posts) {
        this.marker = marker;
        this.posts = posts;
    }

    public final List<PostView> getPosts() {
        return this.posts;
    }

    public void setPosts(final List<PostView> posts) {
        this.posts = posts;
    }

    public final String getMarker() {
        return this.marker;
    }

    public void setMarker(final String marker) {
        this.marker = marker;
    }
}