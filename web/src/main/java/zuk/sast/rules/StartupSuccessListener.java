package zuk.sast.rules;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class StartupSuccessListener {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("StartupSuccessListener onApplicationReady");
        System.out.println("""
                1. 在浏览器器中输入：http://localhost:8080/config
                2. 将在工具的/workspaces/项目名称/parseConfig.json上传
                """);
        EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String args[] = new String[]{"-user", "sa", "-password", "123456", "-url", "jdbc:h2:./data/h2_database"};
                    org.h2.tools.Console.main(args); //启动h2数据库
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
