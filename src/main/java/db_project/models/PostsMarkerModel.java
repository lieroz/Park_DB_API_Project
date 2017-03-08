package db_project.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by lieroz on 8.03.17.
 */

public class PostsMarkerModel {
    private List<PostModel> posts;
    private String marker;

    @JsonCreator
    public PostsMarkerModel(
            @JsonProperty("marker") final String marker,
            @JsonProperty("posts") final List<PostModel> posts
    ) {
        this.marker = marker == null ? "some marker" : marker;
        this.posts = posts;
    }

    public final List<PostModel> getPosts() {
        return this.posts;
    }

    public void setPosts(final List<PostModel> posts) {
        this.posts = posts;
    }

    public final String getMarker() {
        return this.marker;
    }

    public void setMarker(final String marker) {
        this.marker = marker;
    }
}
