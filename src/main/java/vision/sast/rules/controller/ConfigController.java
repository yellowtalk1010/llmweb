package vision.sast.rules.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vision.sast.rules.Database;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
public class ConfigController {

    @PostMapping("upload_config")
    public String upload_config(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return "文件为空！";
        }

        try {
            // 直接读取文件内容
            String content = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
            ).lines().collect(Collectors.joining("\n"));

            String htmlContent = content
                    .replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;")  // 替换制表符为4个空格
                    .replace("\n", "<br>");

            return """
                    <!DOCTYPE html>
                    <html lang="zh-CN">
                    <head>
                      <meta charset="UTF-8">
                      <title>配置文件</title>
                    </head>
                    <body>
                      <h2>配置文件</h2>
                    """
                    +   htmlContent
                    +
                    """
                    </body>
                    </html>
                                        
                    """;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @GetMapping("config")
    public  String config(){
        return """
                       <!DOCTYPE html>
                        <html lang="zh-CN">
                        <head>
                          <meta charset="UTF-8">
                          <title>配置文件</title>
                        </head>
                        <body>
                          <h2>上传配置文件</h2>
                          <form action="/upload_config" method="post" enctype="multipart/form-data">
                            <label for="file">选择文件：</label>
                            <input type="file" name="file" id="file"><br><br>
                            <input type="submit" value="上传">
                          </form>
                        </body>
                        </html>
                                        
                """;
    }

}
