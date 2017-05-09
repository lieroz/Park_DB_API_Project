package db_project.JdbcDAO;

import db_project.DAO.UserDAO;
import db_project.Queries.UserQueries;
import db_project.Views.UserView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lieroz on 9.05.17.
 */
@Component
public class JdbcUserDAO extends JdbcInferiorDAO implements UserDAO {
    public JdbcUserDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public final void create(final String about, final String email, final String fullname, final String nickname) {
        getJdbcTemplate().update(UserQueries.createUserQuery(), about, email, fullname, nickname);
    }

    @Override
    public final void update(final String about, final String email, final String fullname, final String nickname) {
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
        if (!args.isEmpty()) {
            sql.delete(sql.length() - 1, sql.length());
            sql.append(" WHERE nickname = ?");
            args.add(nickname);
            getJdbcTemplate().update(sql.toString(), args.toArray());
        }
    }

    @Override
    public final UserView findSingleByNickOrMail(final String nickname, final String email) {
        return getJdbcTemplate().queryForObject(UserQueries.findUserQuery(), new Object[]{nickname, email}, readUser);
    }

    @Override
    public final List<UserView> findManyByNickOrMail(final String nickname, final String email) {
        return getJdbcTemplate().query(UserQueries.findUserQuery(), new Object[]{nickname, email}, readUser);
    }

    @Override
    public final Integer count() {
        return getJdbcTemplate().queryForObject(UserQueries.countUsersQuery(), Integer.class);
    }

    @Override
    public final void clear() {
        getJdbcTemplate().execute(UserQueries.clearTableQuery());
    }
}
