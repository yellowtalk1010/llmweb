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
        else {
            return "o_o";
        }
    }

}
