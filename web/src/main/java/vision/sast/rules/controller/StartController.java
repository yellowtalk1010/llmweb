package vision.sast.rules.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class StartController {

    private static final List<String> COMMAND_LIST = new ArrayList<>(); //记录运行成功的命令
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private static Boolean IS_RUNNING = false;

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
    public synchronized Map<String, String> runCommand(@RequestBody RunCommandDto runCommandDto) {
//        if(StringUtils.isNotBlank(runCommandDto.getCommand())){
//            StringBuilder stringBuilder = new StringBuilder();
//            Arrays.stream(runCommandDto.getCommand().split(" ")).forEach(s->stringBuilder.append(s + " "));
//        }
        System.out.println(JSON.toJSONString(runCommandDto, JSONWriter.Feature.PrettyFormat));

        if(!IS_RUNNING){
            EXECUTOR_SERVICE.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        IS_RUNNING = true;
                        runProcess(runCommandDto.getCommand());
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    finally {
                        IS_RUNNING = false;
                    }
                }
            });
        }

        Map<String, String> map = new HashMap<>();
        map.put("isRunning", "1");
        return map;
    }


    private static int runProcess(String command) throws Exception {
        List<String> ls = Arrays.stream(command.split(" ")).toList();
        ProcessBuilder processBuilder = new ProcessBuilder(ls);

        processBuilder.redirectErrorStream(false);

        Process process = processBuilder.start();

        // 使用多线程同时读取输出流和错误流，避免阻塞
        StringBuilder outputBuilder = new StringBuilder();
        StringBuilder errorBuilder = new StringBuilder();

        Thread outputThread = new Thread(() -> {
            try (InputStream inputStream = process.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("日志：" + line);
                    outputBuilder.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Thread errorThread = new Thread(() -> {
            try (InputStream errorStream = process.getErrorStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("错误日志：" + line);
                    errorBuilder.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        outputThread.start();
        errorThread.start();

        int exitCode = process.waitFor();

        // 等待读取线程完成
        outputThread.join();
        errorThread.join();

        return exitCode;

    }

}
