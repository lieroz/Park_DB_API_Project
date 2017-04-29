package db_project.services;

import db_project.models.UserViewModel;
import db_project.services.queries.UserQueries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
 * @brief Wrapper on JdbcTemplate for more convenient usage.
 */

@Service
public final class UserService {
    /**
     * @brief Class used for communication with database.
     */
    private final JdbcTemplate jdbcTemplate;

    public UserService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * @brief Add new user to database.
     */

    public final void createUser(@Nullable final String about, @Nullable final String email,
                                       @Nullable final String fullname, @NotNull final String nickname) {
        jdbcTemplate.update(UserQueries.createUserQuery(), about, email, fullname, nickname);
    }

    /**
     * @brief Get information about user.
     */

    public final UserViewModel getUser(@Nullable final String nickname, @Nullable final String email) {
        return jdbcTemplate.queryForObject(UserQueries.getUserQuery(), new Object[]{nickname, email}, UserService::read);
    }

    public final List<UserViewModel> getUsers(@Nullable final String nickname, @Nullable final String email) {
        return jdbcTemplate.query(UserQueries.getUserQuery(), new Object[]{nickname, email}, UserService::read);
    }

    /**
     * @brief Update current information about a specific user.
     */

    public final void updateUser(@Nullable final String about, @Nullable final String email,
                                           @Nullable final String fullname, @NotNull final String nickname) {
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

    /**
     * @brief Serialize database row into UserModel object.
     */

    public static UserViewModel read(ResultSet rs, int rowNum) throws SQLException {
        return new UserViewModel(
                rs.getString("about"),
                rs.getString("email"),
                rs.getString("fullname"),
                rs.getString("nickname")
        );
    }
}
