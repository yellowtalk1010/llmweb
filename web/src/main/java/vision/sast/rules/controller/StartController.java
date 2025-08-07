package vision.sast.rules.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

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

    @Data
    public static class RunCommandDto {
        private String command;
        private String configType;
        private String fileContent;
    }

    @PostMapping("run_command")
    public Map<String, String> runCommand(@RequestBody RunCommandDto runCommandDto) {
        System.out.println(JSON.toJSONString(runCommandDto, JSONWriter.Feature.PrettyFormat));
        Map<String, String> map = new HashMap<>();
        return map;
    }

}
