package vision.sast.rules;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupSuccessListener {

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("""
                1. 在浏览器器中输入：http://localhost:8080/config
                2. 将在工具的/workspaces/项目名称/parseConfig.json上传
                """);
    }
}
