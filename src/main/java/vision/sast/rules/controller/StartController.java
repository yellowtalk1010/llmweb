package vision.sast.rules.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StartController {

    @GetMapping("start")
    public String start(String token) {
        if(token==null || token.isEmpty() || !token.equals(MarioController.RUN_TOKEN)){
            return "启动启动启动，马力欧.奥德赛";
        }
        MarioController.RUN_TOKEN = "";

        return """
                    <!DOCTYPE html>
                    <html lang="zh-CN">
                    <head>
                      <meta charset="UTF-8">
                      <title>问题结果</title>
                    </head>
                    <body>
                       <form action="/start_form">
                       <input type="text" name="projectName" value="" />  <br>
                       
                       <input type="text" name="password" value="" />  <br>
                       <button type="submit" value="启动" />
                       </form>
                    </body>
                    </html>
               """;
    }

}
