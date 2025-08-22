package zuk.sast.rules;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
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
        createNameTable();
    }

    private void createNameTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS name (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                first_name VARCHAR(50) NOT NULL,
                last_name VARCHAR(50) NOT NULL,
                full_name VARCHAR(100) AS (CONCAT(first_name, ' ', last_name)),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status INT DEFAULT 1,
                remark VARCHAR(200),
                CONSTRAINT uk_name_unique UNIQUE (first_name, last_name)
            )
            """;
        jdbcTemplate.execute(sql);
    }

}
