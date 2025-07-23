package vision.sast.rules.controller;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import vision.sast.rules.utils.QRCodeGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RestController
public class QRCongtroller {

    @GetMapping("llm_qr")
    public String llm_qr(){
        String html =
                """
                    <!DOCTYPE html>
                    <html lang="zh-CN">
                    <head>
                      <meta charset="UTF-8">
                      <title>qr</title>
                    </head>
                    <body>
                         <form action="/llm_create_qr" method="post">
                              <input type="radio" name="type" value="1" />路径  <br>
                              <input type="radio" name="type" value="2" checked />内容 <br>
                              <input type="password" name="pwd" /> <br>
                              <textarea name="content" rows="5" cols="50"></textarea>
                              <br>
                              <button  type="submit">查询</button> 
                         </form>
                    </body>
                    </html>
                 """;
        return html;
    }

    @PostMapping("llm_create_qr")
    public String llm_create_qr(Integer type, String content, String pwd) {
        if(pwd==null || pwd.isEmpty()){
            return "error";
        }
        else {
            List<String> list = new ArrayList<String>();
            if(type.equals(1)){
                System.out.println("路径:" + content);
                if(!new File(content).exists()){
                    return "路径不存在";
                }
                list.addAll(headleFile(content));
            }
            else if(type.equals(2)){
                if(content==null || content.isEmpty()){
                    return "内容为空";
                }
                else {
                    System.out.println("内容：xxx");
                    list.addAll(handleString(content));
                }
            }

            System.out.println("路径：");
            list.forEach(System.out::println);

            return """
                hello world.
                """;

        }
    }

    /***
     * 根据文件创建
     * @param file
     */
    private List<String> headleFile(String file) {
        File f = new File(file);
        System.out.println("文件: " + file + ", 是否存在:" + f.exists());
        StringBuilder stringBuilder = new StringBuilder();
        try {
            List<String> lines = FileUtils.readLines(f, "UTF-8");
            lines.stream().map(l->l+"\n").forEach(stringBuilder::append);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return QRCodeGenerator.erweima(stringBuilder.toString(), 900);
    }

    /***
     * 根据内容创建
     * @param string
     */
    private List<String> handleString(String string) {
        System.out.println("内容: " + string);
        return QRCodeGenerator.erweima(string, 900);
    }

}
