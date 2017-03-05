package db_project.services;

import db_project.models.PostModel;
import db_project.models.ThreadModel;
import db_project.models.VoteModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by lieroz on 27.02.17.
 */

/**
 * @brief Wrapper on JdbcTemplate for more convenient usage.
 */

@Service
public class ThreadService {
    /**
     * @brief Class used for communication with database.
     */
    private final JdbcTemplate jdbcTemplate;

    public ThreadService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * @brief Insert multiple posts into database by slug or id.
     */

    public final List<PostModel> insertPostsIntoDb(final List<PostModel> posts, final String slug) {
        Integer id = null;
        Boolean isNumber = Boolean.FALSE;
        final StringBuilder insertRequest = new StringBuilder(
                "INSERT INTO posts (author, created, forum, \"message\", thread, parent) ");
        final StringBuilder getRequest = new StringBuilder("SELECT * FROM posts WHERE thread =");

        try {
            id = Integer.valueOf(slug);
            isNumber = Boolean.TRUE;
            insertRequest.append("VALUES(?, ?, (SELECT forum FROM threads WHERE id = ?), ?, ?, ?)");
            getRequest.append(" ?");

        } catch (NumberFormatException ex) {
            insertRequest.append("VALUES(?, ?, (SELECT forum FROM threads WHERE LOWER(slug) = LOWER(?)), ?," +
                    "(SELECT id FROM threads WHERE LOWER(slug) = LOWER(?)), ?)");
            getRequest.append(" (SELECT id FROM threads WHERE LOWER(slug) = LOWER(?))");
        }

        getRequest.append(" ORDER BY posts.id");

        for (PostModel post : posts) {

            if (post.getCreated() == null) {
                post.setCreated(LocalDateTime.now().toString());
            }

            if (post.getParent() == null) {
                post.setParent(0);
            }

            Timestamp timestamp = Timestamp.valueOf(LocalDateTime.parse(post.getCreated(), DateTimeFormatter.ISO_DATE_TIME));

            if (!post.getCreated().endsWith("Z")) {
                timestamp = Timestamp.from(timestamp.toInstant().plusSeconds(-10800));
            }

            jdbcTemplate.update(insertRequest.toString(), post.getAuthor(), timestamp, isNumber ? id : slug,
                    post.getMessage(), isNumber ? id : slug, post.getParent());

        }

        final List<PostModel> dbPosts = jdbcTemplate.query(getRequest.toString(),
                isNumber ? new Object[]{id} : new Object[]{slug}, PostService::read);
        final Integer beginIndex = dbPosts.size() - posts.size();
        final Integer endIndex = dbPosts.size();

        return dbPosts.subList(beginIndex, endIndex);
    }

    /**
     * @brief Get all information about a specific thread from database.
     */

    public final List<ThreadModel> getThreadInfo(final String slug) {
        final StringBuilder sql = new StringBuilder("SELECT * FROM threads WHERE ");
        final Integer id;

        try {
            id  = Integer.valueOf(slug);

        } catch (NumberFormatException ex) {
            return jdbcTemplate.query(sql.append("LOWER(slug) = LOWER(?)").toString(),
                    new Object[]{slug}, ThreadService::read);
        }

        return jdbcTemplate.query(sql.append("id = ?").toString(),
                new Object[]{id}, ThreadService::read);
    }

    /**
     * @brief Update information about a specific thread from database.
     */

    public final void updateThreadInfoFromDb(final ThreadModel thread, final String slug) {
        final StringBuilder sql = new StringBuilder("UPDATE threads SET");
        final List<Object> args = new ArrayList<>();

        if (thread.getMessage() != null && !thread.getMessage().isEmpty()) {
            sql.append(" message = ?,");
            args.add(thread.getMessage());
        }

        if (thread.getTitle() != null && !thread.getTitle().isEmpty()) {
            sql.append(" title = ?,");
            args.add(thread.getTitle());
        }

        if (args.isEmpty()) {
            return;
        }

        sql.delete(sql.length() - 1, sql.length());
        final Integer id;

        try {
            id = Integer.valueOf(slug);
            sql.append(" WHERE id = ?");
            args.add(id);

        } catch (NumberFormatException ex) {
            sql.append(" WHERE LOWER(slug) = LOWER(?)");
            args.add(slug);
        }

        jdbcTemplate.update(sql.toString(), args.toArray());
    }

    /**
     * @brief Update votes for thread int database.
     */

    private void getUserVotes(final VoteModel vote) {
        final List<VoteModel> usersList = jdbcTemplate.query("SELECT * FROM uservotes " +
                        "WHERE LOWER(nickname) = LOWER(?)",
                new Object[]{vote.getNickname()}, (rs, rowNum) ->
                        new VoteModel(rs.getString("nickname"), rs.getInt("voice")));
        final Map<String, Integer> usersMap = new LinkedHashMap<>();

        for (VoteModel user : usersList) {
            usersMap.put(user.getNickname(), user.getVoice());
        }

        if (usersMap.containsKey(vote.getNickname())) {

            if (usersMap.get(vote.getNickname()) < 0 && vote.getVoice() < 0) {
                vote.setVoice(0);

            } else if (usersMap.get(vote.getNickname()) < 0 && vote.getVoice() > 0) {
                vote.setVoice(2);

            } else if (usersMap.get(vote.getNickname()) > 0 && vote.getVoice() < 0) {
                vote.setVoice(-2);

            } else {
                vote.setVoice(0);
            }

            jdbcTemplate.update("UPDATE uservotes SET voice = voice + ? " +
                    "WHERE LOWER(nickname) = LOWER(?)", vote.getVoice(), vote.getNickname());

        } else {
            jdbcTemplate.update("INSERT INTO uservotes (nickname, voice) VALUES(?, ?)",
                    vote.getNickname(), vote.getVoice());
        }
    }

    public final List<ThreadModel> updateVotes(final VoteModel vote, final String slug) {
        final StringBuilder sql = new StringBuilder("UPDATE threads SET votes = votes + ? WHERE ");
        final List<Object> args = new ArrayList<>();
        final Integer id;
        getUserVotes(vote);
        args.add(vote.getVoice());

        try {
            id = Integer.valueOf(slug);

        } catch (NumberFormatException ex) {
            args.add(slug);
            jdbcTemplate.update(sql.append("LOWER(slug) = LOWER(?)").toString(), args.toArray());

            return jdbcTemplate.query("SELECT * FROM threads WHERE LOWER(slug) = LOWER(?)",
                    new Object[]{slug}, ThreadService::read);
        }

        args.add(id);
        jdbcTemplate.update(sql.append("id = ?").toString(), args.toArray());

        return jdbcTemplate.query("SELECT * FROM threads WHERE id = ?",
                new Object[]{id}, ThreadService::read);
    }


    /**
     * @brief Get information about thread by id.
     */

    public final List<ThreadModel> getThreadInfoById(final Integer id) {
        return jdbcTemplate.query(
                "SELECT * FROM threads WHERE id = ?",
                new Object[]{id},
                ThreadService::read
        );
    }

    /**
     * @brief Serialize database row into ThreadModel object.
     */

    public static ThreadModel read(ResultSet rs, int rowNum) throws SQLException {
        final Timestamp timestamp = rs.getTimestamp("created");
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));

        return new ThreadModel(
                rs.getString("author"),
                dateFormat.format(timestamp),
                rs.getString("forum"),
                rs.getInt("id"),
                rs.getString("message"),
                rs.getString("slug"),
                rs.getString("title"),
                rs.getInt("votes")
        );
    }
}
