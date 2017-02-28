package db_project.services;

import db_project.models.ThreadModel;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by lieroz on 27.02.17.
 */

@Service
public class ThreadService {
    public static ThreadModel readThread(ResultSet rs, int rowNum) throws SQLException {
        return new ThreadModel(
                rs.getString("author"),
                rs.getString("created"),
                rs.getString("forum"),
                rs.getInt("id"),
                rs.getString("message"),
                rs.getString("slug"),
                rs.getString("title"),
                rs.getInt("votes")
        );
    }
}
