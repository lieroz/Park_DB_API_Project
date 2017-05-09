package db_project.DAO;

import db_project.Views.ForumView;
import db_project.Views.ThreadView;
import db_project.Views.UserView;

import java.util.List;

/**
 * Created by lieroz on 9.05.17.
 */
public interface ForumDAO {
    void create(String username, String slug, String title);
    ForumView findBySlug(String slug);
    List<ThreadView> findAllThreads(String slug, Integer limit, String since, Boolean desc);
    List<UserView> findAllUsers(String slug, Integer limit, String since, Boolean desc);
    Integer count();
    void clear();
}
