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
        log.info("开始初始化数据库");
        createProjectTable();
        createIssueTable();
        createStockTable();
        createStockInfoTable();
        log.info("完成初始化数据库");
    }

    private void createProjectTable() {
        log.info("创建项目表");
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
        log.info("创建issue表");
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

    private void createStockTable(){
        log.info("创建stock表");
        String sql = """
            CREATE TABLE IF NOT EXISTS stock (
                id VARCHAR(100) PRIMARY KEY,
                stock_code VARCHAR(100),
                name  VARCHAR(100),
                stock_type VARCHAR(100),
                createtime VARCHAR(100),
                remark TEXT
            )
            """;
        jdbcTemplate.execute(sql);
    }

    private void createStockInfoTable(){
        log.info("创建stock_info表");
        String sql = """
                CREATE TABLE IF NOT EXISTS STOCK_INFO(
                    id VARCHAR(100)  PRIMARY KEY,
                    stock_code  VARCHAR(100),
                    stock_name VARCHAR(100),
                    concept text,
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
        jdbcTemplate.execute(sql);
    }


}
