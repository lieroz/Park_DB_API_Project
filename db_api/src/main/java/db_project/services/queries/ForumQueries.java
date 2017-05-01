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

    public static String createThreadWithTimeQuery() {
        return "INSERT INTO threads (user_id, created, forum_id, message, slug, title) " +
                "  VALUES((SELECT id FROM users WHERE nickname = ?), ?, (SELECT id FROM forums WHERE slug = ?), ?, ?, ?)";
    }

    public static String createThreadWithoutTimeQuery() {
        return "INSERT INTO threads (user_id, forum_id, message, slug, title) " +
                "  VALUES((SELECT id FROM users WHERE nickname = ?), (SELECT id FROM forums WHERE slug = ?), ?, ?, ?)";
    }

    public static String updateThreadsCount() {
        return "UPDATE forums SET threads = threads + 1 WHERE slug = ?";
    }

    public static String getThreadByIdQuery() {
        return "SELECT u.nickname, t.created, f.slug as f_slug, t.id, t.message, t.slug as t_slug, t.title, t.votes " +
                "FROM threads t " +
                "  JOIN users u ON (t.user_id = u.id)" +
                "  JOIN forums f ON (t.forum_id = f.id) " +
                "  WHERE t.id = ?";
    }

    public static String getThreadBySlugQuery() {
        return "SELECT u.nickname, t.created, f.slug as f_slug, t.id, t.message, t.slug as t_slug, t.title, t.votes " +
                "FROM threads t " +
                "  JOIN users u ON (t.user_id = u.id)" +
                "  JOIN forums f ON (t.forum_id = f.id) " +
                "WHERE t.slug = ?";
    }

    public static String getThreadsByForumQuery() {
        return "SELECT u.nickname, t.created, f.slug as f_slug, t.id, t.message, t.slug as t_slug, t.title, t.votes " +
                "FROM threads t " +
                "  JOIN users u ON (t.user_id = u.id)" +
                "  JOIN forums f ON (t.forum_id = f.id) " +
                "  WHERE f.slug = ?";
    }

    public static String getUsersByForumQuery() {
        return "WITH users_table AS (" +
                "  SELECT t.user_id, f1.slug" +
                "  FROM forums f1" +
                "    JOIN threads t ON (t.forum_id = f1.id)" +
                "  UNION" +
                "  SELECT p.user_id, f2.slug" +
                "  FROM forums f2" +
                "    JOIN posts p ON (p.forum_id = f2.id)" +
                ") SELECT u.about, u.email, u.fullname, u.nickname" +
                "  FROM users u" +
                "  WHERE u.id IN (SELECT user_id FROM users_table WHERE slug = ?)";
    }
}
