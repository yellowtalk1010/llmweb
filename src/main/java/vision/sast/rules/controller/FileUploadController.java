package vision.sast.rules.controller;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vision.sast.rules.RulesApplication;
import vision.sast.rules.dto.IssueResult;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@RestController
public class FileUploadController {

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
            System.out.println("收到文件内容：");
            System.out.println(content);
            RulesApplication.ISSUE_RESULT = JSONObject.parseObject(content, IssueResult.class);

            return ResponseEntity.ok("上传成功，issue数量：" + RulesApplication.ISSUE_RESULT.getResult().size());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("读取文件失败！");
        }
    }

}
