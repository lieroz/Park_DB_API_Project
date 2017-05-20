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
        return "SELECT u.nickname, p.created, f.slug, p.id, p.is_edited, p.message, p.parent, p.thread_id " +
                "FROM posts p" +
                "  JOIN users u ON (u.id = p.user_id)" +
                "  JOIN forums f ON (f.id = p.forum_id) " +
                "WHERE p.path[1] IN (" +
                "  SELECT id" +
                "  FROM posts" +
                "  WHERE thread_id = " +
                (slug_or_id.matches("\\d+") ?
                        "?" : "(SELECT threads.id FROM threads WHERE threads.slug = ?)"
                ) +
                "  AND parent = 0 " +
                "  ORDER BY id " + (desc ? "DESC" : "ASC") +
                "  LIMIT ? OFFSET ?) " +
                "ORDER BY path " + (desc ? "DESC" : "ASC");
    }

    public static String countPostsQuery() {
        return "SELECT COUNT(*) FROM posts";
    }

    public static String clearTableQuery() {
        return "DELETE FROM posts";
    }
}
