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

@Service
public class UserService {
    private final JdbcTemplate jdbcTemplate;

    public UserService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public final void insertUserIntoDb(final UserModel user) {
        final String sql = "INSERT INTO Users (about, email, fullname, nickname) VALUES(?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getAbout(), user.getEmail(), user.getFullname(), user.getNickname());
    }

    public final List<UserModel> getUserFromDb(final String nickname) {
        final String sql = "SELECT * FROM Users WHERE nickname = ?";

        return jdbcTemplate.query(sql, new Object[]{nickname}, UserService::read);
    }

    public final void updateUserInfoFromDb(final UserModel user) {
        final StringBuilder sql = new StringBuilder("UPDATE Users SET");
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
        sql.append(" WHERE nickname = ?");
        args.add(user.getNickname());
        jdbcTemplate.update(sql.toString(), args.toArray());
    }

    private static UserModel read(ResultSet rs, int rowNum) throws SQLException {
        return new UserModel(rs.getString("about"), rs.getString("email"),
                rs.getString("fullname"), rs.getString("nickname"));
    }
}
