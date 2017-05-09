package db_project.Queries;

/**
 * Created by lieroz on 28.04.17.
 */
public class ThreadQueries {
    public static String getForumIdQuery() {
        return "SELECT forums.id FROM forums " +
                "JOIN threads ON (threads.forum_id = forums.id) " +
                "WHERE threads.id = ?";
    }

    public static String getThreadId() {
        return "SELECT id FROM threads WHERE slug = ?";
    }

    public static String createThreadWithTimeQuery() {
        return "INSERT INTO threads (user_id, created, forum_id, message, slug, title) " +
                "  VALUES((SELECT id FROM users WHERE nickname = ?), ?, (SELECT id FROM forums WHERE slug = ?), ?, ?, ?) RETURNING id";
    }

    public static String createThreadWithoutTimeQuery() {
        return "INSERT INTO threads (user_id, forum_id, message, slug, title) " +
                "  VALUES((SELECT id FROM users WHERE nickname = ?), (SELECT id FROM forums WHERE slug = ?), ?, ?, ?) RETURNING id";
    }

    public static String updateForumsPostsCount() {
        return "UPDATE forums SET posts = posts + ? WHERE forums.id = ?";
    }

    public static String getThreadQuery(final String slug_or_id) {
        return "SELECT u.nickname, t.created, f.slug AS f_slug, t.id, t.message, t.slug AS t_slug, t.title, t.votes " +
                "FROM threads t " +
                "  JOIN users u ON (t.user_id = u.id)" +
                "  JOIN forums f ON (t.forum_id = f.id) " +
                "  WHERE " + (slug_or_id.matches("\\d+") ? "t.id = ?" : "t.slug = ?");
    }

    public static String updateThreadVotesQuery() {
        return "UPDATE threads SET votes = (SELECT SUM(voice) FROM users WHERE thread_id = ?) WHERE id = ?";
    }

    public static String countThreadsQuery() {
        return "SELECT COUNT(*) FROM threads";
    }

    public static String clearTableQuery() {
        return "DELETE FROM threads";
    }
}
