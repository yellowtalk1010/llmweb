package vision.sast.rules.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vision.sast.rules.Database;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
public class ConfigController {

    private String workspace;
    private String projectName;
    private String resultFilePath;
    private String measureResultFilePath;

    /***
     * 全文检索
     */
    @GetMapping("config_fulltext_index")
    public String config_fulltext_index(){
        String indexDir = this.workspace + "/" + this.projectName + "/indexDir";
        File file = new File(indexDir);
        System.out.println("索引位置:" + indexDir + "，" + file.exists());

        try {
            return """
                    <!DOCTYPE html>
                    <html lang="zh-CN">
                    <head>
                      <meta charset="UTF-8">
                      <title>全文检索</title>
                    </head>
                    <body>
                    <form action="/config_fulltext_search">
                    <textarea name="search" rows="5" cols="50"></textarea>
                    <br>
                    <button  type="submit">检索</button> 
                    </form>
                    </body>
                    </html>
                    """;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    /***
     * 结果路径
     */
    @GetMapping("config_issue_path")
    public String config_issue_path() {
        File file = new File(this.resultFilePath);
        System.out.println("打开结果路径:" + this.resultFilePath + "，" + file.exists());

        try {
            // 直接读取文件内容
            FileInputStream fis = new FileInputStream(file);
            String content = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            Database.buildIssue(this.resultFilePath, content);
            return """
                    <!DOCTYPE html>
                    <html lang="zh-CN">
                    <head>
                      <meta charset="UTF-8">
                      <title>问题结果</title>
                    </head>
                    <body>
                    """
                     + "issue总数：" + Database.ISSUE_RESULT.getResult().size() + "<br>"
                     + "<a href='llm_files'>llm文件集</a><br>"
                     + "<a href='llm_rules'>llm规则集</a><br>"
                     + "<br>"
                     + "<a href='/pages/AllFiles'>文件集</a><br>"
                     + "<a href='/pages/AllRules'>规则集</a><br>"
                     +
                    """
                    </body>
                    </html>
                    """;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }


    }

    @GetMapping("config_measure_path")
    public String config_measure_path() {
        File file = new File(this.measureResultFilePath);
        System.out.println("打开度量路径:" + this.measureResultFilePath + "，" + file.exists());
        try {
            FileInputStream fis = new FileInputStream(file);
            String content = new BufferedReader(
                    new InputStreamReader(fis, StandardCharsets.UTF_8)
            ).lines().collect(Collectors.joining("\n"));
            JSONObject jsonObject = JSONObject.parseObject(content);
            try {
                AtomicInteger count = new AtomicInteger(0);
                JSONObject fileFunctionDefinition = (JSONObject)jsonObject.get("fileFunctionDefinition");
                fileFunctionDefinition.keySet().stream().forEach(key->{
                    JSONArray array = (JSONArray)fileFunctionDefinition.get(key);
                    if(array!=null && array.size()>0){
                        count.addAndGet(array.size());
                    }
                });
                jsonObject.put("fileFunctionDefinition", "总数是：" + count);
            }catch (Exception e){

            }

            String newContent = JSONObject.toJSONString(jsonObject, JSONWriter.Feature.PrettyFormat);

            String htmlContent = newContent
                    .replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;")  // 替换制表符为4个空格
                    .replace("\n", "<br>");

            return """
                    <!DOCTYPE html>
                    <html lang="zh-CN">
                    <head>
                      <meta charset="UTF-8">
                      <title>度量结果</title>
                    </head>
                    <body>
                    <h2>度量结果</h2>
                    """
                    +   htmlContent
                    +
                    """
                    </body>
                    </html>
                                        
                    """;
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @PostMapping("upload_config")
    public String upload_config(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return "文件为空！";
        }

        try {
            // 直接读取文件内容
            String content = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
            ).lines().collect(Collectors.joining("\n"));

            JSONObject json = JSONObject.parseObject(content);
            this.resultFilePath = (String) json.get("resultFilePath");
            this.measureResultFilePath = (String) json.get("measureResultFilePath");
            this.workspace = (String) json.get("workspace");
            this.projectName = (String) json.get("projectName");

            System.out.println("项目名称：" + this.projectName);
            System.out.println("空间路径：" + this.workspace);
            System.out.println("结果路径：" + this.resultFilePath);
            System.out.println("度量路径：" + this.measureResultFilePath);

            String htmlContent = content
                    .replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;")  // 替换制表符为4个空格
                    .replace("\n", "<br>");

            return """
                    <!DOCTYPE html>
                    <html lang="zh-CN">
                    <head>
                      <meta charset="UTF-8">
                      <title>配置文件</title>
                    </head>
                    <body>
                    <h2>结果路径</h2>
                    <a href='config_issue_path'>issue</a><br>
                    <a href='config_measure_path'>measure</a><br>
                   
                    <h2>全文检索</h2>
                    <form action="/config_fulltext_search">
                        <textarea name="search" rows="5" cols="50"></textarea>
                        <br>
                        <button  type="submit">查询</button> 
                    </form>
                    
                    <h2>配置文件</h2>
                    """
                    +   htmlContent
                    +
                    """
                    </body>
                    </html>
                                        
                    """;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @GetMapping("config")
    public  String config(){
        return """
                       <!DOCTYPE html>
                        <html lang="zh-CN">
                        <head>
                          <meta charset="UTF-8">
                          <title>配置文件</title>
                        </head>
                        <body>
                          <h2>上传配置文件</h2>
                          <form action="/upload_config" method="post" enctype="multipart/form-data">
                            <label for="file">选择文件：</label>
                            <input type="file" name="file" id="file"><br><br>
                            <input type="submit" value="上传">
                          </form>
                        </body>
                        </html>
                                        
                """;
    }

}
