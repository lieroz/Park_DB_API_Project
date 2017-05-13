package db_project.Queries;

/**
 * Created by lieroz on 29.04.17.
 */
public class UserQueries {
    public static String createUserQuery() {
        return "INSERT INTO users (about, email, fullname, nickname) VALUES(?, ?, ?, ?)";
    }

    public static String findUserQuery() {
        return "SELECT * FROM users WHERE nickname = ? OR email = ?";
    }

    public static String findUserIdQuery() {
        return "SELECT id FROM users WHERE nickname = ?";
    }

    public static String updateUserVoteQuery() {
        return "UPDATE users SET thread_id = ?, voice = ? WHERE nickname = ?";
    }

    public static String countUsersQuery() {
        return "SELECT COUNT(*) FROM users";
    }

    public static String clearTableQuery() {
        return "DELETE FROM users";
    }
}
