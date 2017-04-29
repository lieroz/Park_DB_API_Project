package db_project.services;

import db_project.models.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Created by lieroz on 3.03.17.
 */

/**
 * @brief Wrapper on JdbcTemplate for more convenient usage.
 */

@Service
public class PostService {
    /**
     * @brief Class used for communication with database.
     */
    private final JdbcTemplate jdbcTemplate;

    public PostService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * @brief Get post by id from database.
     */

    public final List<PostModel> getPostFromDb(final Integer id) {
        final String sql = "SELECT * FROM posts WHERE id = ?";
        return jdbcTemplate.query(
                "SELECT * FROM posts WHERE id = ?",
                new Object[]{id},
                PostService::read);
    }

    /**
     * @brief Get detailed post from database.
     */

    public final PostDetailsModel getDetailedPostFromDb(final PostModel post, String[] related) {
//        UserModel user = null;
//        ForumModel forum = null;
//        ThreadModel thread = null;
//
//        if (related != null) {
//
//            for (String relation : related) {
//
//                if (Objects.equals(relation, "user")) {
//                    UserService userService = new UserService(jdbcTemplate);
//                    List<UserModel> users = userService.getUserFromDb(new UserModel(null, null, null, post.getAuthor()));
//
//                    if (!users.isEmpty()) {
//                        user = users.get(0);
//                    }
//                }
//
//                if (relation.equals("forum")) {
//                    ForumService forumService = new ForumService(jdbcTemplate);
//                    List<ForumModel> forums = forumService.getForumInfo(post.getForum());
//
//                    if (!forums.isEmpty()) {
//                        forum = forums.get(0);
//                    }
//
//                    forum.setThreads(jdbcTemplate.queryForObject(
//                            "SELECT COUNT(*) FROM threads WHERE LOWER(forum) = LOWER(?)",
//                            Integer.class,
//                            forum.getSlug()
//                    ));
//                }
//
//                if (relation.equals("thread")) {
//                    ThreadService forumService = new ThreadService(jdbcTemplate);
//                    List<ThreadModel> threads = forumService.getThreadInfoById(post.getThread());
//
//                    if (!threads.isEmpty()) {
//                        thread = threads.get(0);
//                    }
//                }
//            }
//        }

        return new PostDetailsModel(null, null, null, null);
    }

    /**
     * @brief Update post by id in database.
     */

    public final List<PostModel> updatePostInDb(final PostModel post, final Integer id) {
        final StringBuilder sql = new StringBuilder("UPDATE posts SET \"message\" = ?");
        List<PostModel> posts = getPostFromDb(id);

        if (posts.isEmpty()) {
            return posts;
        }

        if (!Objects.equals(post.getMessage(), posts.get(0).getMessage())) {
            sql.append(", isEdited = TRUE");
        }

        sql.append(" WHERE id = ?");
        jdbcTemplate.update(sql.toString(), post.getMessage(), id);

        return getPostFromDb(id);
    }

    /**
     * @brief Serialize database row into PostModel object.
     */

    public static PostModel read(ResultSet rs, int rowNum) throws SQLException {
        final Timestamp timestamp = rs.getTimestamp("created");
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));

        return new PostModel(
                rs.getString("author"),
                dateFormat.format(timestamp),
                rs.getString("forum"),
                rs.getInt("id"),
                rs.getBoolean("isEdited"),
                rs.getString("message"),
                rs.getInt("parent"),
                rs.getInt("thread")
        );
    }
}
