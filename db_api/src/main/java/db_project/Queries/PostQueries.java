package db_project.Queries;

/**
 * Created by lieroz on 1.05.17.
 */
public class PostQueries {
    public static String createPostsQuery() {
        return "INSERT INTO posts (user_id, created, forum_id, id, message, parent, thread_id, path) VALUES(" +
                "(SELECT id FROM users WHERE nickname = ?), ?, ?, ?, ?, ?, ?, " +
                "array_append((SELECT path FROM posts WHERE id = ?), ?))";
    }

    public static String getPostQuery() {
        return "SELECT u.nickname, p.created, f.slug, p.id, p.is_edited, p.message, p.parent, p.thread_id " +
                "FROM posts p" +
                "  JOIN users u ON (u.id = p.user_id)" +
                "  JOIN forums f ON (f.id = p.forum_id) " +
                "WHERE p.id = ?";
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
        return "SELECT u.nickname, p.created, f.slug, p.id, p.is_edited, p.message, p.parent, p.thread_id " +
                "FROM posts p" +
                "  JOIN users u ON (u.id = p.user_id)" +
                "  JOIN forums f ON (f.id = p.forum_id) " +
                "WHERE p.thread_id = " +
                (slug_or_id.matches("\\d+") ?
                        "?" : "(SELECT threads.id FROM threads WHERE threads.slug = ?)"
                ) +
                " ORDER BY path " + (desc ? "DESC" : "ASC") + " LIMIT ? OFFSET ?";
    }

    public static String postsParentTreeSortQuery(final String slug_or_id, final Boolean desc) {
        return "WITH RECURSIVE some_posts AS (" +
                "    SELECT u.nickname, p.created, f.slug, p.id, p.is_edited, p.message, p.parent, p.thread_id, p.path" +
                "    FROM posts p" +
                "      JOIN users u ON (u.id = p.user_id)" +
                "      JOIN forums f ON (f.id = p.forum_id) " +
                "    WHERE p.thread_id = " +
                (slug_or_id.matches("\\d+") ?
                        "?" : "(SELECT threads.id FROM threads WHERE threads.slug = ?)"
                ) +
                "), tree AS (" +
                "    (" +
                "      SELECT *" +
                "      FROM some_posts" +
                "        WHERE parent = 0" +
                "      ORDER BY id " + (desc ? "DESC" : "ASC") +
                "      LIMIT ? OFFSET ?" +
                "    )" +
                "    UNION ALL" +
                "    (" +
                "      SELECT" +
                "        some_posts.*" +
                "      FROM tree" +
                "        JOIN some_posts ON some_posts.parent = tree.id" +
                "      WHERE some_posts.path && tree.path" +
                "    )" +
                ")" +
                "SELECT * FROM tree ORDER BY path " + (desc ? "DESC" : "ASC");
    }

    public static String countPostsQuery() {
        return "SELECT COUNT(*) FROM posts";
    }

    public static String clearTableQuery() {
        return "DELETE FROM posts";
    }
}
