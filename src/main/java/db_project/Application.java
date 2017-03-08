package db_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by lieroz on 23.02.17.
 */

/*
 * TODO Sortings of posts: tree/flat
 * TODO Invalid post parent
 * TODO Users sortings
 */

@SpringBootApplication
public class Application {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
