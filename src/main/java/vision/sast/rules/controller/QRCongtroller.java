package vision.sast.rules.controller;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import vision.sast.rules.utils.QRCodeGenerator;

import java.io.File;
import java.util.List;

@Controller
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
                    <form action="/llm_create_qr">
                         <input type="radio" name="type" value="1" />路径  <br/>
                         <input type="radio" name="type" value="2" checked />内容 <br/>
                         <textarea name="search" rows="5" cols="100" /> <br>
                         <button  type="submit">查询</button> 
                    </form>
               </body>
               </html>
            """;

        return html;
    }

    @PostMapping("llm_create_qr")
    public String llm_create_qr(Integer type, String content, String pwd) {
        if(pwd==null || pwd.isEmpty() || pwd.toLowerCase().equals("mario")){
            return "error";
        }
        else {
            if(type.equals(1)){
                System.out.println("路径:" + content);
                if(!new File(content).exists()){
                    return "路径不存在";
                }
                headleFile(content);
            }
            else if(type.equals(2)){
                System.out.println("内容：xxx");
                handleString(content);
            }

        }
        return "";
    }

    /***
     * 根据文件创建
     * @param file
     */
    private void headleFile(String file) {
        File f = new File(file);
        System.out.println("文件: " + file + ", 是否存在:" + f.exists());
        StringBuilder stringBuilder = new StringBuilder();
        try {
            List<String> lines = FileUtils.readLines(f, "UTF-8");
            lines.stream().map(l->l+"\n").forEach(stringBuilder::append);
        }catch (Exception e) {
            e.printStackTrace();
        }
        QRCodeGenerator.erweima(stringBuilder.toString(), 900);
    }

    /***
     * 根据内容创建
     * @param string
     */
    private void handleString(String string) {
        System.out.println("内容: " + string);
        QRCodeGenerator.erweima(string, 900);
    }

}
