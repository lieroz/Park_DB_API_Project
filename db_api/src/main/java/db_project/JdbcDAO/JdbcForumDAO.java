package db_project.JdbcDAO;

import db_project.DAO.ForumDAO;
import db_project.Queries.ForumQueries;
import db_project.Views.ForumView;
import db_project.Views.ThreadView;
import db_project.Views.UserView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lieroz on 9.05.17.
 */
@Service
public class JdbcForumDAO extends JdbcInferiorDAO implements ForumDAO {
    public JdbcForumDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void create(String username, String slug, String title) {
        getJdbcTemplate().update(ForumQueries.createForumQuery(), username, slug, title);
    }

    @Override
    public ForumView findBySlug(String slug) {
        return getJdbcTemplate().queryForObject(ForumQueries.getForumQuery(), new Object[]{slug}, readForum);
    }

    @Override
    public List<ThreadView> findAllThreads(String slug, Integer limit, String since, Boolean desc) {
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
    public List<UserView> findAllUsers(String slug, Integer limit, String since, Boolean desc) {
        final Integer forumId = getJdbcTemplate().queryForObject("SELECT id FROM forums WHERE slug = ?", Integer.class, slug);
        final StringBuilder sql = new StringBuilder(ForumQueries.getUsersByForumQuery());
        final List<Object> args = new ArrayList<>();
        args.add(forumId);
        if (since != null) {
            sql.append(" AND u.nickname ");
            sql.append(desc == Boolean.TRUE ? "< ?" : "> ?");
            args.add(since);
        }
        sql.append(" ORDER BY u.nickname COLLATE ucs_basic");
        sql.append(desc == Boolean.TRUE ? " DESC" : "");
        sql.append(" LIMIT ?");
        args.add(limit);
        return getJdbcTemplate().query(sql.toString(), args.toArray(), readUser);
    }

    @Override
    public Integer count() {
        return getJdbcTemplate().queryForObject(ForumQueries.countForumsQuery(), Integer.class);
    }

    @Override
    public void clear() {
        getJdbcTemplate().execute(ForumQueries.clearTableQuery());
    }
}
