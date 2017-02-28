package db_project.services;

import db_project.models.ThreadModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by lieroz on 27.02.17.
 */

@Service
public class ThreadService {
    private final JdbcTemplate jdbcTemplate;

    public ThreadService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public final List<ThreadModel> getThreadInfo(final String slug) {
        return jdbcTemplate.query(
                "SELECT * FROM Threads WHERE slug = ?",
                new Object[]{slug},
                ThreadService::read
        );
    }

    public static ThreadModel read(ResultSet rs, int rowNum) throws SQLException {
        return new ThreadModel(
                rs.getString("author"),
                rs.getString("created"),
                rs.getString("forum"),
                rs.getInt("id"),
                rs.getString("message"),
                rs.getString("slug"),
                rs.getString("title"),
                rs.getInt("votes")
        );
    }
}
