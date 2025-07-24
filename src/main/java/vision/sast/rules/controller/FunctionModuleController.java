package vision.sast.rules.controller;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import vision.sast.rules.FunctionModuleDatabase;
import vision.sast.rules.dto.IssueDto;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController()
public class FunctionModuleController {

    @Data
    private static class FuncModuleRequestDto {
        private String issueId;
        private List<String> paramValues;
    }

    @PostMapping("handle_func_module")
    public Map<String, String> handle_func_module(@RequestBody FuncModuleRequestDto funcModuleRequestDto) {
        System.out.println("函数输入输出:" + JSON.toJSONString(funcModuleRequestDto));
        Map<String, String> map = new HashMap<>();
        map.put("status", "失败");
        if(funcModuleRequestDto.getIssueId()!=null && StringUtils.isNotBlank(funcModuleRequestDto.getIssueId())){
            IssueDto issueDto = FunctionModuleDatabase.queryIssueDtoById(funcModuleRequestDto.getIssueId());
            if(issueDto!=null){
                Object object = issueDto.getData();
                System.out.println();
            }
        }
        return map;
    }

    @GetMapping("func_module_path")
    public String func_module_path(String path){
        File file = new File(path);
        if(!file.exists()){
            return "函数模型路径不存在:" + path;
        }
        else {
            try {
                FunctionModuleDatabase.initFunctionModuleDatabase(path);

                StringBuilder stringBuilder = new StringBuilder("<li><a href='pages/AllFiles?vtid="+ FunctionModuleDatabase.FunctionModuleVtid+"'>函数建模</a></li>");
                FunctionModuleDatabase.queryAllFiles().stream().forEach(e->{
                    stringBuilder.append("<li>"+e+"</li>");
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
