package zuk.sast.rules;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static zuk.sast.rules.MybatisH2DatabaseConfig.DATABASE_FILE_URL;

@Slf4j
@Component
public class StartupSuccessListener {



    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("StartupSuccessListener onApplicationReady");
        System.out.println("""
                1. 在浏览器器中输入：http://localhost:8080/config
                2. 将在工具的/workspaces/项目名称/parseConfig.json上传
                """);

    }
}
