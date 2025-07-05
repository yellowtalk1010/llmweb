package vision.sast.rules.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vision.sast.rules.Database;

import java.util.UUID;


@RestController
public class PagesController {

    public static String RUN_TOKEN = "";

    /***
     * 全部页面
     * @return
     */
    @GetMapping("pages")
    public String pages() {
        RUN_TOKEN = UUID.randomUUID().toString();
        String config_url  = "config";
        String start_url = "start?token=" + RUN_TOKEN;
        String html =
                """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                  <meta charset="UTF-8">
                  <title>页面列表</title>
                </head>
                <body>
                    <ul>
                        <li><a style="text-decoration: none;" href='{{{config_url}}}'>🔥上传配置文件🔥</a></li>
                        <br>
                        <li><a style="text-decoration: none;" href='{{{start_url}}}'>👨‍🔧启动🚀启动🚀🚀启动🚀🚀🚀奥利给💪💪💪💪</a></li>
                    <ul> 
                </body>
                </html>
                """;
        html = html.replace("{{{config_url}}}", config_url);
        html = html.replace("{{{start_url}}}", start_url);
        return html;
    }

}