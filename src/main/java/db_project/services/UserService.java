package db_project.services;

import db_project.models.UserModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lieroz on 23.02.17.
 */

/**
 * @ brief Wrapper on JdbcTemplate for more convenient usage.
 */

@Service
public final class UserService {
    /**
     * @ brief Class used for communication with database.
     */
    private final JdbcTemplate jdbcTemplate;

    public UserService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * @ brief Add new user to database.
     */

    public final void insertUserIntoDb(final UserModel user) {
        final String sql = "INSERT INTO users (about, email, fullname, nickname) " +
                "VALUES(?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getAbout(), user.getEmail(), user.getFullname(),
                user.getNickname());
    }

    /**
     * @ brief Get information about user.
     */

    public final List<UserModel> getUserFromDb(final UserModel user) {
        return jdbcTemplate.query(
                "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?)" +
                        "OR LOWER(email) = LOWER(?)",
                new Object[]{user.getNickname(), user.getEmail()},
                UserService::read);
    }

    /**
     * @ brief Update current information about a specific user.
     */

    public final void updateUserInfoFromDb(final UserModel user) {
        final StringBuilder sql = new StringBuilder("UPDATE users SET");
        final List<Object> args = new ArrayList<>();

        if (user.getAbout() != null && !user.getAbout().isEmpty()) {
            sql.append(" about = ?,");
            args.add(user.getAbout());
        }

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            sql.append(" email = ?,");
            args.add(user.getEmail());
        }

        if (user.getFullname() != null && !user.getFullname().isEmpty()) {
            sql.append(" fullname = ?,");
            args.add(user.getFullname());
        }

        if (args.isEmpty()) {
            return;
        }

        sql.delete(sql.length() - 1, sql.length());
        sql.append(" WHERE LOWER(nickname) = LOWER(?)");
        args.add(user.getNickname());
        jdbcTemplate.update(sql.toString(), args.toArray());
    }

    /**
     * @ brief Serialize database row into UserModel object.
     */

    public static UserModel read(ResultSet rs, int rowNum) throws SQLException {
        return new UserModel(
                rs.getString("about"),
                rs.getString("email"),
                rs.getString("fullname"),
                rs.getString("nickname")
        );
    }
}
