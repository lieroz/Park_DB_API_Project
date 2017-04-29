package db_project.services.queries;

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

    public static String createThreadQuery() {
        return "INSERT INTO threads (user_id, created, forum_id, message, slug, title) " +
                "  VALUES((SELECT id FROM users WHERE nickname = ?), ?, (SELECT id FROM forums WHERE slug = ?), ?, ?, ?)";
    }

    public static String updateThreadsCount() {
        return "UPDATE forums SET threads = threads + 1 WHERE slug = ?";
    }

    public static String getThreadQuery() {
        return "SELECT t.created, t.id, f.slug, t.title, t.message, u.nickname, t.votes " +
                "FROM threads t " +
                "  JOIN users u ON (t.user_id = u.id)" +
                "  JOIN forums f ON (t.forum_id = f.id) " +
                "WHERE f.slug = ?";
    }
}
