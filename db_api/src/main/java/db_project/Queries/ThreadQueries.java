package db_project.Queries;

/**
 * Created by lieroz on 28.04.17.
 */
public class ThreadQueries {
    public static String getForumIdAndSlugQuery() {
        return "SELECT forums.id FROM forums " +
                "JOIN threads ON (threads.forum_id = forums.id) " +
                "WHERE threads.id = ?";
    }

    public static String getThreadId() {
        return "SELECT id FROM threads WHERE slug = ?";
    }

    public static String createPostsQuery() {
        return "INSERT INTO posts (user_id, created, forum_id, id, message, parent, thread_id) VALUES(" +
                "(SELECT id FROM users WHERE nickname = ?), ?, ?, ?, ?, ?, ?)";
    }

    public static String checkPostParentQuery() {
        return "SELECT * FROM check_post_parent(?)";
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

    public static String updateUserVoteQuery() {
        return "UPDATE users SET thread_id = ?, voice = ? WHERE nickname = ?";
    }

    public static String updateThreadVotesQuery() {
        return "UPDATE threads SET votes = (SELECT SUM(voice) FROM users WHERE thread_id = ?) WHERE id = ?";
    }

    public static String postsFlatSortQuery(final String slug_or_id, final Boolean desc) {
        return "SELECT u.nickname, p.created, f.slug, p.id, p.is_edited, p.message, p.parent, p.thread_id " +
                "FROM posts p" +
                "  JOIN users u ON (u.id = p.user_id)" +
                "  JOIN forums f ON (f.id = p.forum_id) " +
                "WHERE p.thread_id = " +
                    (slug_or_id.matches("\\d+")
                        ? "?" : "(SELECT threads.id FROM threads WHERE threads.slug = ?)") +
                " ORDER BY p.created " + (desc ? "DESC" : "ASC") + ", p.id " + (desc ? "DESC" : "ASC") +
                " LIMIT ? OFFSET ?";
    }

    public static String postsTreeSortQuery(final String slug_or_id, final Boolean desc) {
        return "WITH RECURSIVE some_posts AS (" +
                "    SELECT u.nickname, p.created, f.slug, p.id, p.is_edited, p.message, p.parent, p.thread_id" +
                "    FROM posts p" +
                "      JOIN users u ON (u.id = p.user_id)" +
                "      JOIN forums f ON (f.id = p.forum_id) " +
                "      JOIN threads t ON (t.id = p.thread_id)" +
                "    WHERE t.id = " +
                        (slug_or_id.matches("\\d+") ?
                                "?" : "(SELECT threads.id FROM threads WHERE threads.slug = ?)"
                        ) +
                "), tree AS (" +
                "    (" +
                "      SELECT *, array[id] AS path" +
                "      FROM some_posts" +
                "        WHERE parent = 0" +
                "    )" +
                "    UNION" +
                "    (" +
                "      SELECT" +
                "        some_posts.*," +
                "        array_append(tree.path, some_posts.id) AS path" +
                "      FROM tree" +
                "        JOIN some_posts ON some_posts.parent = tree.id" +
                "    )" +
                ")" +
                "SELECT * FROM tree ORDER BY path " + (desc ? "DESC" : "ASC") + " LIMIT ? OFFSET ?";
    }

    public static String postsParentTreeSortQuery(final String slug_or_id, final Boolean desc) {
        return "WITH RECURSIVE some_posts AS (" +
                "    SELECT u.nickname, p.created, f.slug, p.id, p.is_edited, p.message, p.parent, p.thread_id" +
                "    FROM posts p" +
                "      JOIN users u ON (u.id = p.user_id)" +
                "      JOIN forums f ON (f.id = p.forum_id) " +
                "      JOIN threads t ON (t.id = p.thread_id)" +
                "    WHERE t.id = " +
                        (slug_or_id.matches("\\d+") ?
                                "?" : "(SELECT threads.id FROM threads WHERE threads.slug = ?)"
                        ) +
                "), tree AS (" +
                "    (" +
                "      SELECT *, array[id] AS path" +
                "      FROM some_posts" +
                "        WHERE parent = 0" +
                "      ORDER BY id " + (desc ? "DESC" : "ASC") +
                "      LIMIT ? OFFSET ?" +
                "    )" +
                "    UNION" +
                "    (" +
                "      SELECT" +
                "        some_posts.*," +
                "        array_append(tree.path, some_posts.id) AS path" +
                "      FROM tree" +
                "        JOIN some_posts ON some_posts.parent = tree.id" +
                "    )" +
                ")" +
                "SELECT * FROM tree ORDER BY path " + (desc ? "DESC" : "ASC");
    }
}
