package db_project.DAO;

import db_project.Views.UserView;

import java.util.List;

/**
 * Created by lieroz on 9.05.17.
 */
public interface UserDAO {
    UserView insert(String about, String email, String fullname, String nickname);
    void update(String about, String email, String fullname, String nickname);
    UserView findSingleByNickOrMail(String nickname, String email);
    List<UserView> findManyByNickOrMail(String nickname, String email);
    Integer count();
    void clear();
}
