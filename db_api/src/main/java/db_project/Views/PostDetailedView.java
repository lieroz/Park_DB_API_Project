package db_project.Views;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lieroz on 9.05.17.
 */
public class PostDetailedView {
    private UserView author;
    private ForumView forum;
    private PostView post;
    private ThreadView thread;

    public PostDetailedView(@JsonProperty("author") final UserView author,
                            @JsonProperty("forum") final ForumView forum,
                            @JsonProperty("post") final PostView post,
                            @JsonProperty("thread") final ThreadView thread) {
        this.author = author;
        this.forum = forum;
        this.post = post;
        this.thread = thread;
    }

    public final UserView getAuthor() {
        return this.author;
    }

    public void setAuthor(final UserView author) {
        this.author = author;
    }

    public final ForumView getForum() {
        return this.forum;
    }

    public void setForum(ForumView forum) {
        this.forum = forum;
    }

    public final PostView getPost() {
        return this.post;
    }

    public void setPost(PostView post) {
        this.post = post;
    }

    public final ThreadView getThread() {
        return this.thread;
    }

    public void setThread(ThreadView thread) {
        this.thread = thread;
    }
}
