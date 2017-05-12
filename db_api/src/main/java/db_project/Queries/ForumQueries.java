package db_project.Queries;

/**
 * Created by lieroz on 29.04.17.
 */
public class ForumQueries {
    public static String createForumQuery() {
        return "INSERT INTO forums (user_id, slug, title) VALUES((SELECT id FROM users WHERE nickname = ?), ?, ?)";
    }

    public static String getForumQuery() {
        return "SELECT f.posts, f.slug, f.threads, f.title, u.nickname " +
                "FROM forums f " +
                "  JOIN users u ON (f.user_id = u.id)" +
                "  WHERE f.slug = ?";
    }

    public static String updateThreadsCountQuery() {
        return "UPDATE forums SET threads = threads + 1 WHERE slug = ?";
    }

    public static String getThreadsByForumQuery() {
        return "SELECT u.nickname, t.created, f.slug as f_slug, t.id, t.message, t.slug as t_slug, t.title, t.votes " +
                "FROM threads t " +
                "  JOIN users u ON (t.user_id = u.id)" +
                "  JOIN forums f ON (t.forum_id = f.id) " +
                "  WHERE f.slug = ?";
    }

    public static String getUsersByForumQuery() {
        return "SELECT u.about, u.email, u.fullname, u.nickname " +
                "FROM users u " +
                "WHERE u.id IN (" +
                "  SELECT user_id " +
                "  FROM forum_users " +
                "  WHERE forum_id = ?" +
                ")";
    }

    public static String countForumsQuery() {
        return "SELECT COUNT(*) FROM forums";
    }

    public static String clearTableQuery() {
        return "DELETE FROM forums";
    }
}
