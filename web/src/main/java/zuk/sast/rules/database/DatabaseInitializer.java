package zuk.sast.rules.database;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库初始化
 * */
@Slf4j
@Component
public class DatabaseInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        createProjectTable();
        createIssueTable();
    }

    private void createProjectTable() {
        //创建项目表
        String sql = """
            CREATE TABLE IF NOT EXISTS project (
                id VARCHAR(100) PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                content BLOB NOT NULL,
                createdTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        jdbcTemplate.execute(sql);
    }


    private void createIssueTable(){
        //创建issue表
        String sql = """
            CREATE TABLE IF NOT EXISTS issue (
                id VARCHAR(100) PRIMARY KEY,
                project_id  VARCHAR(100),
                num BIGINT NOT NULL,
                content BLOB NOT NULL
            )
            """;
        jdbcTemplate.execute(sql);
    }


}
