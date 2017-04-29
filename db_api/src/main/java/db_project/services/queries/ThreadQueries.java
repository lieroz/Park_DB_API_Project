package db_project.services.queries;

/**
 * Created by lieroz on 28.04.17.
 */
public class ThreadQueries {
    public static String postsFlatSortQuery(final String slug_or_id, final Boolean desc) {
        return "SELECT * FROM posts WHERE posts.thread = " +
                    (slug_or_id.matches("\\d+")
                        ? "?" : "(SELECT threads.id FROM threads WHERE LOWER(threads.slug) = LOWER(?))") +
                " ORDER BY posts.created " + (desc ? "DESC" : "ASC") + ", posts.id " + (desc ? "DESC" : "ASC") +
                " LIMIT ? OFFSET ?";
    }

    public static String postsTreeSortQuery(final String slug_or_id, final Boolean desc) {
        return "WITH RECURSIVE some_posts AS (" +
                "    SELECT posts.*" +
                "    FROM posts" +
                "      JOIN threads ON posts.thread = threads.id" +
                "        WHERE threads.id = " +
                            (slug_or_id.matches("\\d+") ?
                                "?" : "(SELECT threads.id FROM threads WHERE LOWER(threads.slug) = LOWER(?))"
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
                "    SELECT posts.*" +
                "    FROM posts" +
                "      JOIN threads ON posts.thread = threads.id" +
                "        WHERE threads.id = " +
                            (slug_or_id.matches("\\d+") ?
                                "?" : "(SELECT threads.id FROM threads WHERE LOWER(threads.slug) = LOWER(?))"
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
