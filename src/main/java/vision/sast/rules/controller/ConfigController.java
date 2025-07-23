package vision.sast.rules.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vision.sast.rules.IssueDatabase;
import vision.sast.rules.utils.LuceneUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
public class ConfigController {

    private String workspace; //工作空间
    private String projectName; //项目名称
    private String resultFilePath; //issue存储路径
    private String measureResultFilePath; //度量结果存储路径
    private String systemConstraintPath; //系统约束路径
    private String INDEXS; //全文检索路径
    private String FUNCTIONMODULE; //函数建模路径

    /***
     * 全文检索
     */
    @GetMapping("config_fulltext_search")
    public String config_fulltext_search(String search){

        File file = new File(INDEXS);
        System.out.println("索引位置:" + INDEXS + "，" + file.exists());
        if(!file.exists()){
            return "索引库不存在";
        }
        System.out.println("查询数据:" + search);

        try {
            List<LuceneUtil.IndexDto> indexDtoList = LuceneUtil.search(search, INDEXS);
            System.out.println(indexDtoList.size());

//            String json = JSONObject.toJSONString(indexDtoList);
//            System.out.println(json);

            AtomicInteger atomicInteger = new AtomicInteger(0);
            StringBuilder stringBuilder = new StringBuilder();
            indexDtoList.stream().forEach(indexDto -> {
                String filepath = indexDto.getFilePath().replaceAll("\\\\", "/");
                List<LuceneUtil.HighlightDto> dtos = indexDto.getHighlightDtos();
                atomicInteger.addAndGet(dtos.size());
                dtos.stream().forEach(e->{
                    int lineNum = e.getLineNum();
                    String lineStr = "<a href='llm_sourcecode?file="+filepath+"&line="+e.getLineNum()+"'>" + e.getLineStr() + "</a>";
                    String html = """
                           <tr>
                                <td>
                                {{{filepath}}}
                                </td>
                                <td>
                                {{{lineNum}}}
                                </td>
                                <td>
                                {{{lineStr}}}
                                </td>
                           </tr>
                           """;
                    html = html.replace("{{{filepath}}}", filepath);
                    html = html.replace("{{{lineNum}}}", String.valueOf(lineNum));
                    html = html.replace("{{{lineStr}}}", lineStr);

                    stringBuilder.append(html);
                });

            });

            System.out.println("总行数：" + atomicInteger.get());

            if(StringUtils.isEmpty(stringBuilder.toString())){
                return "无";
            }
            else {
                return """
                    <!DOCTYPE html>
                    <html lang="zh-CN">
                    <head>
                      <meta charset="UTF-8">
                      <title>全文检索</title>
                        <style>
                          table {
                            width: 80%;
                            border-collapse: collapse;
                            margin: 20px auto;
                          }
                          th, td {
                            border: 1px solid #333;
                            padding: 8px 12px;
                            text-align: left;
                          }
                          th {
                            background-color: #f2f2f2;
                          }
                          tr:hover {
                            background-color: #f9f9f9;
                          }
                        </style>
                    </head>
                    <body>
                    <table style="width: 100%; border-collapse: collapse; table-layout: auto;">
                    <tbody>
                    """
                        +
                        stringBuilder.toString()
                        +
                        """
                        </tbody>
                        </table>
                        </body>
                        </html>
                        """;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    /***
     * 系统约束
     */
    @GetMapping("config_systemConstraint_path")
    public String config_systemConstraint_path(){
        File file = new File(systemConstraintPath);
        if(!file.exists()){
            return file.getAbsolutePath() + "，路径不存在";
        }
        else {
            try {
                List<String> list = FileUtils.readLines(file, "UTF-8");
                StringBuilder stringBuilder = new StringBuilder();

                Map<String, List<Map<String, String>>> groupedMaps = list.stream().map(line->{
                    Map<String, String> map = JSON.parseObject(line, Map.class);
                    map.remove("checkType");
                    map.remove("defectLevel");
                    map.remove("defectType");
                    map.remove("rule");
                    map.remove("issueDesc");
                    map.remove("vtId");
                    map.remove("traces");
                    return map;
                }).collect(Collectors.groupingBy(m->m.get("filePath")));

                groupedMaps.entrySet().stream().forEach(entry->{
                    String filePath = entry.getKey();
                    stringBuilder.append("<li><font color='red'>"+filePath+"</font></li>");
                    List<Map<String, String>> values = entry.getValue().stream().sorted(Comparator.comparing(m->Integer.valueOf(String.valueOf(m.get("line"))))).collect(Collectors.toList());
                    values.stream().forEach(v->{
                        String line = String.valueOf(v.get("line"));
                        String ruleDesc = v.get("ruleDesc");
                        String name = v.get("name");
                        stringBuilder.append("<li>"+line + "行，" + name + "，" + ruleDesc +"</li>");
                    });

                });

                String html = """
                    <!DOCTYPE html>
                    <html lang="zh-CN">
                    <head>
                      <meta charset="UTF-8">
                      <title>系统约束</title>
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
//                return file.getAbsolutePath();
            }catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }
    }

    /***
     * 结果路径
     */
    @GetMapping("config_issue_path")
    public String config_issue_path() {

        try {
            // 直接读取文件内容
            IssueDatabase.initIssues(this.resultFilePath);
            String html = """
                    <!DOCTYPE html>
                    <html lang="zh-CN">
                    <head>
                      <meta charset="UTF-8">
                      <title>问题结果</title>
                    </head>
                    <body>
                    """
                     + "issue总数：" + IssueDatabase.getAllIssue().size() + "<br>"
                     + "<a href='llm_files'>llm文件集</a><br>"
                     + "<a href='llm_rules'>llm规则集</a><br>"
                     + "<br>"
                     + "<a href='/pages/AllRules'>酷洛米</a><br>"
                     + "<a href='mario'>Mario</a><br>"
                     + "<br>"
                     + "<a href='func_module_path?path={{{{FUNCTIONMODULE}}}}'>塞尔达传说</a><br>"
                     + "<br>"
                     + "<a href='llm_qr'>switch2</a><br>"
                     +
                    """
                    </body>
                    </html>
                    """;
            if(this.FUNCTIONMODULE!=null){
                html = html.replace("{{{{FUNCTIONMODULE}}}}", FUNCTIONMODULE);
            }
            return html;
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
            String fileContent = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
            ).lines().collect(Collectors.joining("\n"));

            JSONObject json = JSONObject.parseObject(fileContent);
            this.resultFilePath = (String) json.get("resultFilePath");
            this.measureResultFilePath = (String) json.get("measureResultFilePath");
            this.workspace = (String) json.get("workspace");
            this.projectName = (String) json.get("projectName");
            this.systemConstraintPath = (String) json.get("systemConstraintPath"); //系统约束
            this.INDEXS = this.workspace + "/" + this.projectName + "/zuk/INDEXS"; //项目代码全文检索路径
            this.FUNCTIONMODULE = this.workspace + "/" + this.projectName + "/zuk/FUNCTIONMODULE/functionModule.jsonl"; //项目代码全文检索路径

            System.out.println("项目名称：" + this.projectName);
            System.out.println("工作空间：" + this.workspace);
            System.out.println("结果路径：" + this.resultFilePath);
            System.out.println("度量路径：" + this.measureResultFilePath);
            System.out.println("系统约束：" + this.systemConstraintPath);
            System.out.println("索引路径：" + this.INDEXS);
            System.out.println("函数建模：" + this.FUNCTIONMODULE);

            String htmlFileContent = fileContent
                    .replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;")  // 替换制表符为4个空格
                    .replace("\n", "<br>");

            String html = """
                    <!DOCTYPE html>
                    <html lang="zh-CN">
                    <head>
                      <meta charset="UTF-8">
                      <title>配置文件</title>
                    </head>
                    <body>
                    <h2>结果路径</h2>
                    <a href='config_issue_path'>issue</a>&nbsp;&nbsp;&nbsp;{{{resultFilePath}}}<br>
                    <a href='config_measure_path'>measure</a>&nbsp;&nbsp;&nbsp;{{{measureResultFilePath}}}<br>
                    
                    <h2>系统约束</h2>
                    <a href='config_systemConstraint_path'>systemConstraint</a>&nbsp;&nbsp;&nbsp;{{{systemConstraintPath}}}<br>
                   
                    <h2>全文检索</h2>
                    {{{INDEXS}}}
                    <br>
                    <form action="/config_fulltext_search">
                        <textarea name="search" rows="5" cols="50"></textarea>
                        <br>
                        <button  type="submit">查询</button> 
                    </form>
                    
                    
                    
                    <h2>配置文件</h2>
                    {{{htmlFileContent}}}
                    </body>
                    </html>
                                        
                    """;
            html = html.replace("{{{resultFilePath}}}", resultFilePath);
            if(this.measureResultFilePath!=null){
                html = html.replace("{{{measureResultFilePath}}}", measureResultFilePath);
            }
            if(this.systemConstraintPath!=null){
                html = html.replace("{{{systemConstraintPath}}}", systemConstraintPath + "&nbsp;&nbsp;&nbsp;" + new File(systemConstraintPath).exists());
            }


            html = html.replace("{{{INDEXS}}}", INDEXS + "&nbsp;&nbsp;&nbsp;" + new File(INDEXS).exists());
            html = html.replace("{{{htmlFileContent}}}", htmlFileContent);

            return html;
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

}
