package db_project.DAO;

import db_project.Views.ThreadView;
import db_project.Views.VoteView;

/**
 * Created by lieroz on 9.05.17.
 */
public interface ThreadDAO {
    ThreadView create(String author, String created, String forum, String message, String slug, String title);
    void update(String message, String title, String slug_or_id);
    ThreadView findByIdOrSlug(String slug_or_id);
    ThreadView updateVotes(VoteView view, String slug_or_id);
    Integer count();
    void clear();
}
