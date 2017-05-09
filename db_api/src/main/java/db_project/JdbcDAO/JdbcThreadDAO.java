package db_project.JdbcDAO;

import db_project.DAO.ThreadDAO;
import db_project.Queries.ForumQueries;
import db_project.Queries.ThreadQueries;
import db_project.Queries.UserQueries;
import db_project.Views.ThreadView;
import db_project.Views.VoteView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lieroz on 9.05.17.
 */
@Component
public class JdbcThreadDAO extends JdbcInferiorDAO implements ThreadDAO {
    public JdbcThreadDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public final ThreadView create(final String author, final String created, final String forum,
                                   final String message, final String slug, final String title) {
        final Integer threadId;
        if (created != null) {
            threadId = getJdbcTemplate().queryForObject(ThreadQueries.createThreadWithTimeQuery(),
                    new Object[]{author, created, forum, message, slug, title}, Integer.class);
        } else {
            threadId = getJdbcTemplate().queryForObject(ThreadQueries.createThreadWithoutTimeQuery(),
                    new Object[]{author, forum, message, slug, title}, Integer.class);
        }
        getJdbcTemplate().update(ForumQueries.updateThreadsCountQuery(), forum);
        return getJdbcTemplate().queryForObject(ThreadQueries.getThreadQuery(threadId.toString()),
                new Object[]{threadId}, readThread);
    }

    @Override
    public final void update(final String message, final String title, final String slug_or_id) {
        final StringBuilder sql = new StringBuilder("UPDATE threads SET");
        final List<Object> args = new ArrayList<>();
        if (message != null) {
            sql.append(" message = ?,");
            args.add(message);
        }
        if (title != null) {
            sql.append(" title = ?,");
            args.add(title);
        }
        if (!args.isEmpty()) {
            sql.delete(sql.length() - 1, sql.length());
            sql.append(slug_or_id.matches("\\d+") ? " WHERE id = ?" : " WHERE slug = ?");
            args.add(slug_or_id);
            getJdbcTemplate().update(sql.toString(), args.toArray());
        }
    }

    @Override
    public final ThreadView findByIdOrSlug(final String slug_or_id) {
        return getJdbcTemplate().queryForObject(ThreadQueries.getThreadQuery(slug_or_id), new Object[]{slug_or_id}, readThread);
    }

    @Override
    public final ThreadView updateVotes(final VoteView view, final String slug_or_id) {
        getJdbcTemplate().queryForObject(UserQueries.findUserQuery(), new Object[]{view.getNickname(), null}, readUser);
        final Integer threadId = slug_or_id.matches("\\d+") ? Integer.valueOf(slug_or_id) :
                getJdbcTemplate().queryForObject(ThreadQueries.getThreadId(), Integer.class, slug_or_id);
        getJdbcTemplate().update(UserQueries.updateUserVoteQuery(), threadId, view.getVoice(), view.getNickname());
        getJdbcTemplate().update(ThreadQueries.updateThreadVotesQuery(), threadId, threadId);
        return getJdbcTemplate().queryForObject(ThreadQueries.getThreadQuery(slug_or_id), new Object[]{slug_or_id}, readThread);
    }

    @Override
    public final Integer count() {
        return getJdbcTemplate().queryForObject(ThreadQueries.countThreadsQuery(), Integer.class);
    }

    @Override
    public final void clear() {
        getJdbcTemplate().execute(ThreadQueries.clearTableQuery());
    }
}
