package vision.sast.rules;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import vision.sast.rules.dto.IssueResult;
import vision.sast.rules.utils.PropertiesKey;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Properties;

@SpringBootApplication
public class RulesApplication {

    public static String ISSUE_FILEPATH;
    public static IssueResult ISSUE_RESULT;
    public static Properties PROPERTIES = new Properties();

    public static void main(String[] args) {

        //System.setProperty("java.awt.headless", "true");


        if(args!=null && args.length>0){
            String issuePath = args[0];
            ISSUE_RESULT = buildIssueResult(new File(issuePath));
        }
        if(ISSUE_RESULT==null){
            System.out.println("issue result is null");
            System.exit(0);
        }
        loadProperties();
        SpringApplication.run(RulesApplication.class, args);

    }

    public static void loadProperties() {
        try {
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

    public static IssueResult buildIssueResult(File file){
        if(file!=null && file.exists()){
            try {
                ISSUE_FILEPATH = file.getAbsolutePath().replaceAll("\\\\", "/");
                System.out.println(ISSUE_FILEPATH + ", " + file.exists());
                StringBuilder stringBuilder = new StringBuilder();
                FileUtils.readLines(file, Charset.forName("utf-8")).stream().map(line->line+"\n").forEach(stringBuilder::append);
                IssueResult issueResult = JSONObject.parseObject(stringBuilder.toString(), IssueResult.class);

                return issueResult;
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else {
            System.out.println("issue file is not exist");
            System.exit(0);
        }
        return new IssueResult();
    }

}
