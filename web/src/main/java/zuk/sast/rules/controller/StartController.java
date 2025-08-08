package zuk.sast.rules.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import zuk.sast.rules.webSocket.LogSocketHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
public class StartController {

    private static final Set<String> COMMAND_SET = new HashSet<>(); //记录运行成功的命令
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private static Boolean IS_RUNNING = false;

    private static volatile String LOG_FORMAT = "UTF-8";
    private static volatile String ERROR_FORMAT = "UTF-8";

    @GetMapping("command_list")
    public synchronized Map<String, Object> command_list(){
        Map<String, Object> map = new HashMap<>();
//        List<String> list = new ArrayList<>();
//        list.add("java -jar D:/AAAAAAAAAAAAAAAAAAAA/github/engine/vision/target/visionSAST.jar -config D:/AAAAAAAAAAAAAAAAAAAA/github/engine/vision/target/workspace1/CJ2000A/project.json");
        map.put("commands", COMMAND_SET);
        return map;
    }


    @Data
    public static class RunCommandDto {
        private String command;
    }

    @GetMapping("command_format")
    public synchronized Map<String, Object> command_format(String tab, String format){
        log.info("tab:" + tab + ", format:" + format);
        if(StringUtils.isNotBlank(tab) && StringUtils.isNotBlank(format)){
            if(tab.equals("log")){
                LOG_FORMAT = format;
            }
            if(tab.equals("error")){
                ERROR_FORMAT = format;
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("log", LOG_FORMAT);
        map.put("error", ERROR_FORMAT);
        return map;
    }

    @PostMapping("run_command")
    public synchronized Map<String, String> runCommand(@RequestBody RunCommandDto runCommandDto) {

        log.info(JSON.toJSONString(runCommandDto, JSONWriter.Feature.PrettyFormat));
        if(StringUtils.isNotEmpty(runCommandDto.getCommand())){
            COMMAND_SET.add(runCommandDto.getCommand());
        }

        if(!IS_RUNNING){
            EXECUTOR_SERVICE.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        IS_RUNNING = true;
                        int exitCode = runProcess(runCommandDto.getCommand());
                        log.info("exitCode:" + exitCode);
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
        List<String> ls = Arrays.stream(command.split(" ")).filter(e->StringUtils.isNotEmpty(e)).toList();
        ProcessBuilder processBuilder = new ProcessBuilder(ls);

        processBuilder.redirectErrorStream(false);

        Process process = processBuilder.start();

        // 使用多线程同时读取输出流和错误流，避免阻塞
        //StringBuilder outputBuilder = new StringBuilder();
        //StringBuilder errorBuilder = new StringBuilder();

        Thread outputThread = new Thread(() -> {
            try (InputStream inputStream = process.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, LOG_FORMAT))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(LOG_FORMAT + "日志：" + line);
                    //outputBuilder.append(line).append("\n");
                    LogSocketHandler.pushMessage(line, "info");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Thread errorThread = new Thread(() -> {
            try (InputStream errorStream = process.getErrorStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream, ERROR_FORMAT))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(ERROR_FORMAT + "错误日志：" + line);
                    //errorBuilder.append(line).append("\n");
                    LogSocketHandler.pushMessage(line, "error");
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
