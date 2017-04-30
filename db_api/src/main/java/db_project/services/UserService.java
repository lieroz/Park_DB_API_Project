package db_project.services;

import db_project.models.UserViewModel;
import db_project.services.queries.UserQueries;
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
public final class UserService {
    private final JdbcTemplate jdbcTemplate;

    public UserService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public final void createUser(final String about, final String email, final String fullname, final String nickname) {
        jdbcTemplate.update(UserQueries.createUserQuery(), about, email, fullname, nickname);
    }

    public final UserViewModel getUser(final String nickname, final String email) {
        return jdbcTemplate.queryForObject(UserQueries.getUserQuery(), new Object[]{nickname, email}, UserService::read);
    }

    public final List<UserViewModel> getUsers(final String nickname, final String email) {
        return jdbcTemplate.query(UserQueries.getUserQuery(), new Object[]{nickname, email}, UserService::read);
    }

    public final void updateUser(final String about, final String email, final String fullname, final String nickname) {
        final StringBuilder sql = new StringBuilder("UPDATE users SET");
        final List<Object> args = new ArrayList<>();

        if (about != null) {
            sql.append(" about = ?,");
            args.add(about);
        }

        if (email != null) {
            sql.append(" email = ?,");
            args.add(email);
        }

        if (fullname != null) {
            sql.append(" fullname = ?,");
            args.add(fullname);
        }

        if (args.isEmpty()) {
            return;
        }

        sql.delete(sql.length() - 1, sql.length());
        sql.append(" WHERE nickname = ?");
        args.add(nickname);
        jdbcTemplate.update(sql.toString(), args.toArray());
    }

    public static UserViewModel read(ResultSet rs, int rowNum) throws SQLException {
        return new UserViewModel(
                rs.getString("about"),
                rs.getString("email"),
                rs.getString("fullname"),
                rs.getString("nickname")
        );
    }
}
