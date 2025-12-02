package zuk.sast.rules;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "zuk.sast")
public class RulesApplication {

    public static void main(String[] args) {

        System.getProperties().put("file.encoding", "UTF-8");
        System.getProperties().put("spring.servlet.multipart.max-file-size", 1024*10 + "MB"); //上传文件最大10G
        System.getProperties().put("spring.servlet.multipart.max-request-size", 1024*10 + "MB"); //上传文件最大10G

        SpringApplication.run(RulesApplication.class, args);

    }


}
