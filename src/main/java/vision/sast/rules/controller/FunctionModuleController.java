package vision.sast.rules.controller;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.io.FileUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;
import java.util.Map;

@RestController("")
public class FunctionModuleController {

    @GetMapping("func_module_path")
    public String func_module_path(String path){
        File file = new File(path);
        if(!file.exists()){
            return "函数模型路径不存在:" + path;
        }
        else {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                List<String> list = FileUtils.readLines(new File(path), "UTF-8");
                list.forEach(line->{
                    Map<String, String> map = JSON.parseObject(line,Map.class);
                    stringBuilder.append(JSON.toJSONString(map) + "<br>");
                });
                return stringBuilder.toString();
            }catch (Exception e){
                return e.getMessage();
            }
        }

    }
}
