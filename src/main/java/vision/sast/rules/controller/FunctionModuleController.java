package vision.sast.rules.controller;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.io.FileUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController("")
public class FunctionModuleController {

    private static List<Map<String, String>> MAP = new ArrayList<>();

    private static String createKey(Map<String, String> map) {
        String filePath =  map.get("filePath");
        String line = String.valueOf(map.get("line"));
        return filePath + ":" + line;
    }

    @GetMapping("func_module_path")
    public String func_module_path(String path){
        MAP.clear();
        File file = new File(path);
        if(!file.exists()){
            return "函数模型路径不存在:" + path;
        }
        else {
            try {
                List<String> list = FileUtils.readLines(new File(path), "UTF-8");
                Set<String> paths = new HashSet<>();
                list.forEach(jsonline->{
                    Map<String, String> map = JSON.parseObject(jsonline, Map.class);
                    MAP.add(map);

                    String filePath =  map.get("filePath");
//                    String line = String.valueOf(map.get("line"));
//                    String key = createKey(map);
                    paths.add(filePath);
                });

                StringBuilder stringBuilder = new StringBuilder();
                paths.stream().forEach(e->{
                    stringBuilder.append("<li><a href='pages/AllFiles?vtid=FunctionModule&file="+e+"'>" + e + "</a></li>");
                });

                String html = """
                    <!DOCTYPE html>
                    <html lang="zh-CN">
                    <head>
                      <meta charset="UTF-8">
                      <title>函数模型</title>
                    </head>
                    <body>
                        <ul>
                            {{{stringBuilder}}}
                        </ul>
                    </body>
                    </html>
                        """;

                html = html.replace("{{{stringBuilder}}}", stringBuilder.toString());

                return html;
            }catch (Exception e){
                return e.getMessage();
            }
        }

    }



}
