package vision.sast.rules;

import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import vision.sast.rules.controller.IssueResultController;
import vision.sast.rules.dto.IssueResult;
import vision.sast.rules.utils.PropertiesKey;

import java.io.*;
import java.util.Properties;

@SpringBootApplication
@ComponentScan(basePackages = "vision.sast")
public class RulesApplication {

    //issue文件路径
    @Deprecated
    public static String ISSUE_FILEPATH = ""; //统一改为文件上传的方式
    //issue结果保存
    public static IssueResult ISSUE_RESULT = new IssueResult();
    //property中文件加载
    public static Properties PROPERTIES = new Properties();

    public static void main(String[] args) {

        System.getProperties().put("file.encoding", "UTF-8");
        System.getProperties().put("spring.servlet.multipart.max-file-size", 1024*10 + "MB"); //上传文件最大10G
        System.getProperties().put("spring.servlet.multipart.max-request-size", 1024*10 + "MB"); //上传文件最大10G

        if(args.length > 0){
            String issuePath = args[0];
            if(new File(issuePath).exists()){
                try {
                    String content = FileUtils.readFileToString(new File(issuePath), "UTF-8");
                    IssueResultController.buildIssue(content);
                    ISSUE_FILEPATH = issuePath;
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        }

        loadProperties();
        SpringApplication.run(RulesApplication.class, args);

    }

    /***
     * 加载config.properties文件
     */
    public static void loadProperties() {
        try {
            PROPERTIES.clear();
            String configFileName = "config.properties";
            File propertiesFile = new File(configFileName);
            if(!propertiesFile.exists()){
                try (FileOutputStream out = new FileOutputStream(propertiesFile)) {
                    PropertiesKey.properties.store(out, "配置文件 - 用户信息");
                    System.out.println("写入完成！");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            InputStream input = new FileInputStream(propertiesFile);
            PROPERTIES.load(input);
            System.out.println(PROPERTIES);
        }catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
