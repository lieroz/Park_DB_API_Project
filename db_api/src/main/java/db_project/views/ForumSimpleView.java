package db_project.views;

/**
 * Created by lieroz on 30.04.17.
 */
public class ForumSimpleView {
    private String forumSlug;
    private Integer forumId;

    public ForumSimpleView(final String forumSlug, final Integer forumId) {
        this.forumSlug = forumSlug;
        this.forumId = forumId;
    }

    public final String getForumSlug() {
        return this.forumSlug;
    }

    public final Integer getForumId() {
        return this.forumId;
    }
}
