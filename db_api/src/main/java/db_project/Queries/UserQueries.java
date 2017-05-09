package db_project.Queries;

/**
 * Created by lieroz on 29.04.17.
 */
public class UserQueries {
    public static String getUserIdQuery() {
        return "SELECT id FROM users WHERE nickname = ?";
    }

    public static String createUserQuery() {
        return "INSERT INTO users (about, email, fullname, nickname) VALUES(?, ?, ?, ?)";
    }

    public static String getUserQuery() {
        return "SELECT * FROM users WHERE nickname = ? OR email = ?";
    }
}
