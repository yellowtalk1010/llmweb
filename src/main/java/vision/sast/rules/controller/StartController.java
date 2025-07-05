package vision.sast.rules.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StartController {

    @GetMapping("start")
    public String start(String token) {
        System.out.println("token:" + token);
        return "启动启动启动";
    }

}
