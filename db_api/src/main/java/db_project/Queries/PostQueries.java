package db_project.Queries;

/**
 * Created by lieroz on 1.05.17.
 */
public class PostQueries {
    public static String getPostQuery() {
        return "SELECT u.nickname, p.created, f.slug, p.id, p.is_edited, p.message, p.parent, p.thread_id " +
                "FROM posts p" +
                "  JOIN users u ON (u.id = p.user_id)" +
                "  JOIN forums f ON (f.id = p.forum_id) " +
                "WHERE p.id = ?";
    }
}
