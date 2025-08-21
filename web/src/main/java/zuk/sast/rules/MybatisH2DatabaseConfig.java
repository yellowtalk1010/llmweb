package zuk.sast.rules;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
@Configuration
@MapperScan("zuk.sast.rules.controller.mapper") // 指定 MyBatis Mapper 接口所在的包
public class MybatisH2DatabaseConfig {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    static {
        EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    log.info("启动数据库");
                    String args[] = new String[]{"-user", "sa", "-password", "123456", "-url", DATABASE_FILE_URL};
                    org.h2.tools.Console.main(args); //启动h2数据库
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public static final String DATABASE_FILE_URL = "jdbc:h2:./data/h2_database";  //文件数据库

    @Bean
    public DataSource dataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(DATABASE_FILE_URL);
        dataSource.setUser("sa");
        dataSource.setPassword("123456");
        tryDatabase(dataSource);
        return dataSource;
    }

    private void tryDatabase(JdbcDataSource dataSource){
        try {
            Connection connection = dataSource.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();

            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            List<String> tableNames = new ArrayList<>();
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                tableNames.add(tableName);
            }
            log.info("数据库表名：" + String.join(", ", tableNames));

        }
        catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }


    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        return sessionFactory.getObject();
    }

//    @Bean
//    public PlatformTransactionManager transactionManager(DataSource dataSource) {
//        return new DataSourceTransactionManager(dataSource);
//    }

}
