package db_project.JdbcDAO;

import db_project.DAO.ForumDAO;
import db_project.Queries.ForumQueries;
import db_project.Views.ForumView;
import db_project.Views.ThreadView;
import db_project.Views.UserView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lieroz on 9.05.17.
 */
@Component
public class JdbcForumDAO extends JdbcInferiorDAO implements ForumDAO {
    public JdbcForumDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public final void create(String username, String slug, String title) {
        final String sql = "INSERT INTO forums (user_id, slug, title) VALUES((SELECT id FROM users WHERE nickname = ?), ?, ?)";
        getJdbcTemplate().update(sql, username, slug, title);
    }

    @Override
    public final ForumView findBySlug(String slug) {
        return getJdbcTemplate().queryForObject(ForumQueries.getForumQuery(), new Object[]{slug}, readForum);
    }

    @Override
    public final List<ThreadView> findAllThreads(String slug, Integer limit, String since, Boolean desc) {
        final StringBuilder sql = new StringBuilder(ForumQueries.getThreadsByForumQuery());
        final List<Object> args = new ArrayList<>();
        args.add(slug);
        if (since != null) {
            sql.append(" AND t.created ");
            sql.append(desc == Boolean.TRUE ? "<= ?" : ">= ?");
            args.add(since);
        }
        sql.append(" ORDER BY t.created");
        sql.append(desc == Boolean.TRUE ? " DESC" : "");
        sql.append(" LIMIT ?");
        args.add(limit);
        return getJdbcTemplate().query(sql.toString(), args.toArray(new Object[args.size()]), readThread);
    }

    @Override
    public final List<UserView> findAllUsers(String slug, Integer limit, String since, Boolean desc) {
        final StringBuilder sql = new StringBuilder(ForumQueries.getUsersByForumQuery());
        final List<Object> args = new ArrayList<>();
        args.add(slug);
        args.add(slug);
        if (since != null) {
            sql.append(" AND u.nickname ");
            sql.append(desc == Boolean.TRUE ? "< ?" : "> ?");
            args.add(since);
        }
        sql.append(" GROUP BY u.about, u.email, u.fullname, u.nickname");
        sql.append(" ORDER BY u.nickname COLLATE ucs_basic");
        sql.append(desc == Boolean.TRUE ? " DESC" : "");
        sql.append(" LIMIT ?");
        args.add(limit);
        return getJdbcTemplate().query(sql.toString(), args.toArray(), readUser);
    }

    @Override
    public final Integer count() {
        return getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM forums", Integer.class);
    }

    @Override
    public final void clear() {
        getJdbcTemplate().execute("DELETE FROM forums");
    }
}
