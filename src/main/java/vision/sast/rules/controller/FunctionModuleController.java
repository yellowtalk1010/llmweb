package vision.sast.rules.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vision.sast.rules.FunctionModuleDatabase;

import java.io.File;

@RestController()
public class FunctionModuleController {

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
