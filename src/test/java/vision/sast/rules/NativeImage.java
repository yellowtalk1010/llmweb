package vision.sast.rules;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/***
 * 采用 native image 命令将jar转成exe程序
 * 1. 运行你的程序以收集反射信息（收集到 native-image 文件夹中）：
 *    java -agentlib:native-image-agent=config-output-dir=./native-image -jar visionSAST.jar
 * 2. 读取 ./native-image/reflect-config.json
 * 3. 生产一个新的 reflect-config.json 文件
 * 4. 通过native image 生成 visonRules.exe
 *    .\engine\vision> native-image -cp .\target\visionSAST.jar -H:Class=vision.sast.VisionMain --no-fallback --enable-http --enable-https -H:ConfigurationFileDirectories=src/main/resources/native-image  -H:Name=.\target\visionRules
 * 5. 运行 visionRules.exe
 */
public class NativeImage {

    public static void main(String[] args) throws Exception {
        String path = "D:\\development\\github\\engine\\vision\\src\\main\\resources\\native-image";
        File reflectConfigFile = new File(path + File.separator + "reflect-config.json");
        System.out.println(reflectConfigFile.getAbsolutePath());
        assert reflectConfigFile.exists();
        String text = FileUtils.readFileToString(reflectConfigFile, "UTF-8");
        List<Dto> list = JSON.parseArray(text, Dto.class);
        System.out.print(list.size());
        list.forEach(dto->{
            System.out.println(dto.getName());
        });
        String json = JSONObject.toJSONString(list, JSONWriter.Feature.PrettyFormat);
        System.out.println(json);
        FileUtils.writeStringToFile(new File("reflect-config.json"), json, "UTF-8");
        System.out.println("完成");
    }

    @Data
    private static class Dto {
        private String name;
        private boolean allDeclaredConstructors = true;
        private boolean allPublicConstructors = true;
        private boolean allDeclaredMethods = true;
        private boolean allPublicMethods = true;
    }

}
