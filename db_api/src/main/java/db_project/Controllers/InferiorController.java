package db_project.Controllers;

import db_project.JdbcDAO.JdbcForumDAO;
import db_project.JdbcDAO.JdbcPostDAO;
import db_project.JdbcDAO.JdbcThreadDAO;
import db_project.JdbcDAO.JdbcUserDAO;
import db_project.Views.StatusView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lieroz on 9.05.17.
 */
@RestController
@RequestMapping("api/service")
public class InferiorController {
    @Autowired
    protected JdbcUserDAO jdbcUserDAO;
    @Autowired
    protected JdbcForumDAO jdbcForumDAO;
    @Autowired
    protected JdbcThreadDAO jdbcThreadDAO;
    @Autowired
    protected JdbcPostDAO jdbcPostDAO;

    @RequestMapping("/status")
    public final ResponseEntity<Object> serverStatus() {
        final Integer forumsCount = jdbcForumDAO.count();
        final Integer postsCount = jdbcPostDAO.count();
        final Integer threadsCount = jdbcThreadDAO.count();
        final Integer usersCount = jdbcUserDAO.count();
        return ResponseEntity.status(HttpStatus.OK).body(new StatusView(forumsCount, postsCount, threadsCount, usersCount));
    }

    @RequestMapping("/clear")
    public final ResponseEntity<Object> clearService() {
        jdbcPostDAO.clear();
        jdbcThreadDAO.clear();
        jdbcForumDAO.clear();
        jdbcUserDAO.clear();
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
