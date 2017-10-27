package db_project.Queries;

/**
 * Created by lieroz on 1.05.17.
 */
public class PostQueries {
    public static String createPostsQuery() {
        return "INSERT INTO posts (user_id, created, forum_id, id, message, parent, thread_id, path, root_id) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, array_append(?, ?), ?)";
    }

    public static String insertIntoForumUsers() {
        return "INSERT INTO forum_users (user_id, forum_id) VALUES (?, ?)";
    }

    public static String getPostQuery() {
        return "SELECT u.nickname, p.created, f.slug, p.id, p.is_edited, p.message, p.parent, p.thread_id " +
                "FROM posts p" +
                "  JOIN users u ON (u.id = p.user_id)" +
                "  JOIN forums f ON (f.id = p.forum_id) " +
                "WHERE p.id = ?";
    }

    public static String getPostsFlat(final Integer limit, final Integer since, final Boolean desc ) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT u.nickname, p.created, f.slug, p.id, p.is_edited, p.message, p.parent, p.thread_id ");
        builder.append("FROM users u JOIN posts p ON (u.id = p.user_id) ");
        builder.append("JOIN forums f ON (f.id = p.forum_id) ");
        builder.append("WHERE p.thread_id = ? ");
        String order = (desc == Boolean.TRUE ? " DESC " : " ASC ");
        String sign = (desc == Boolean.TRUE ? " < " : " > ");
        if (since != null) {
            builder.append(" AND p.id").append(sign).append("? ");
        }
        builder.append("ORDER BY p.id ").append(order);
        if (limit != null) {
            builder.append("LIMIT ?");
        }
        return builder.toString();
    }

    public static String getPostsTree(final Integer limit, final Integer since, final Boolean desc ) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT u.nickname, p.created, f.slug, p.id, p.is_edited, p.message, p.parent, p.thread_id ");
        builder.append("FROM users u JOIN posts p ON (u.id = p.user_id) ");
        builder.append("JOIN forums f ON (f.id = p.forum_id) ");
        builder.append("WHERE p.thread_id = ? ");
        String order = (desc == Boolean.TRUE ? " DESC " : " ASC ");
        String sign = (desc == Boolean.TRUE ? " < " : " > ");
        if (since != null) {
            builder.append(" AND p.path ").append(sign).append("(SELECT path FROM posts WHERE id = ?) ");
        }
        builder.append("ORDER BY p.path ").append(order);
        if (limit != null) {
            builder.append("LIMIT ?");
        }
        return builder.toString();
    }

    public static String getPostsParentTree(final Integer limit, final Integer since, final Boolean desc ) {
        String order = (desc == Boolean.TRUE ? " DESC " : " ASC ");
        String sign = (desc == Boolean.TRUE ? " < " : " > ");
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT u.nickname, p.created, f.slug, p.id, p.is_edited, p.message, p.parent, p.thread_id ");
        builder.append("FROM users u JOIN posts p ON (u.id = p.user_id) ");
        builder.append("JOIN forums f ON (f.id = p.forum_id) ");
        builder.append("WHERE p.root_id IN (SELECT id FROM posts WHERE thread_id = ? AND parent = 0 ");
        if (since != null) {
            builder.append(" AND path ").append(sign).append("(SELECT path FROM posts WHERE id = ?) ");
        }
        builder.append("ORDER BY id ").append(order);
        if (limit != null) {
            builder.append(" LIMIT ?");
        }
        builder.append(") ");
        builder.append("ORDER BY p.path ").append(order);
        return builder.toString();
    }

    public static String countPostsQuery() {
        return "SELECT COUNT(*) FROM posts";
    }

    public static String clearTableQuery() {
        return "DELETE FROM posts";
    }
}
