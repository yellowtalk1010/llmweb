package vision.sast.rules.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class StartController {

    private static final List<String> COMMAND_LIST = new ArrayList<>(); //记录运行成功的命令

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
//        if(StringUtils.isNotBlank(runCommandDto.getCommand())){
//            StringBuilder stringBuilder = new StringBuilder();
//            Arrays.stream(runCommandDto.getCommand().split(" ")).forEach(s->stringBuilder.append(s + " "));
//        }
        System.out.println(JSON.toJSONString(runCommandDto, JSONWriter.Feature.PrettyFormat));
        Map<String, String> map = new HashMap<>();
        return map;
    }

}
