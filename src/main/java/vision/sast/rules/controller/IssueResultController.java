package vision.sast.rules.controller;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vision.sast.rules.RulesApplication;
import vision.sast.rules.dto.IssueResult;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class IssueResultController {

    /***
     * 获取issue结果文件信息
     */
    @GetMapping("getIssueResult")
    public Map getIssueResult(){
        System.out.println("getIssueResult");
        Map<String, Object> map = new HashMap<>();
        map.put("issueResultFilePath", RulesApplication.ISSUE_FILEPATH);
        map.put("issueNum", RulesApplication.ISSUE_RESULT.getResult().size());
        return map;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件为空！");
        }

        try {
            // 直接读取文件内容
            String content = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
            ).lines().collect(Collectors.joining("\n"));

            // 可以在这里处理 content，比如解析、打印、返回等
//            System.out.println("收到文件内容：");
//            System.out.println(content);

            RulesApplication.ISSUE_FILEPATH = "";
            RulesApplication.ISSUE_RESULT = JSONObject.parseObject(content, IssueResult.class);
            RulesApplication.loadProperties();

            System.out.println(RulesApplication.ISSUE_RESULT.getResult().size());

            RuleController.clear();
            FileController.clear();
            SourceCodeController.clear();

            return ResponseEntity.ok("上传成功，issue数量：" + RulesApplication.ISSUE_RESULT.getResult().size());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("读取文件失败！");
        }
    }

}
