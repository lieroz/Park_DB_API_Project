package db_project.DAO;

import db_project.Views.PostDetailedView;
import db_project.Views.PostView;
import db_project.Views.ThreadView;

import java.util.List;

/**
 * Created by lieroz on 9.05.17.
 */
public interface PostDAO {
    void create(List<PostView> posts, String slug_or_id);
    PostView update(String message, Integer id);
    PostView findById(Integer id);
    PostDetailedView detailedView(Integer id, String[] related);
    List<PostView> sort(ThreadView thread, String slug_or_id, Integer limit, Integer since, String sort, Boolean desc);
    Integer count();
    void clear();
}
