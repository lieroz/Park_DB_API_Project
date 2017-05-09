package db_project.JdbcDAO;

import db_project.Views.ForumView;
import db_project.Views.PostView;
import db_project.Views.ThreadView;
import db_project.Views.UserView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by lieroz on 9.05.17.
 */
public class JdbcInferiorDAO extends JdbcDaoSupport {
    @Autowired
    public JdbcInferiorDAO(JdbcTemplate jdbcTemplate) {
        setJdbcTemplate(jdbcTemplate);
    }

    protected RowMapper<UserView> readUser = (rs, rowNum) ->
            new UserView(rs.getString("about"), rs.getString("email"),
                    rs.getString("fullname"), rs.getString("nickname"));

    protected RowMapper<ForumView> readForum = (rs, rowNum) ->
            new ForumView(rs.getInt("posts"), rs.getString("slug"),
                    rs.getInt("threads"), rs.getString("title"), rs.getString("nickname"));

    protected RowMapper<ThreadView> readThread = (rs, rowNum) -> {
        final Timestamp timestamp = rs.getTimestamp("created");
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return new ThreadView(rs.getString("nickname"), dateFormat.format(timestamp.getTime()),
                rs.getString("f_slug"), rs.getInt("id"), rs.getString("message"),
                rs.getString("t_slug"), rs.getString("title"), rs.getInt("votes"));
    };

    protected RowMapper<PostView> readPost = (rs, rowNum) -> {
        final Timestamp timestamp = rs.getTimestamp("created");
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return new PostView(rs.getString("nickname"), dateFormat.format(timestamp),
                rs.getString("slug"), rs.getInt("id"), rs.getBoolean("is_edited"),
                rs.getString("message"), rs.getInt("parent"), rs.getInt("thread_id"));
    };
}
