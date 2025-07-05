package vision.sast.rules.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vision.sast.rules.Database;


@RestController
public class PagesController {

    /***
     * 全部页面
     * @return
     */
    @GetMapping("pages")
    public String pages() {
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                  <meta charset="UTF-8">
                  <title>页面列表</title>
                </head>
                <body>
                    <ul>
                        <li><a style="text-decoration: none;" href='config'>上传配置文件🔥</a></li>
                        <br>
                        <li><a style="text-decoration: none;" href='start'>启动🚀启动🚀启动🚀奥利给💪</a>👨‍🔧👨‍🔧👨‍🔧👨‍🔧</li>
                    <ul> 
                </body>
                </html>
                """;
    }

}