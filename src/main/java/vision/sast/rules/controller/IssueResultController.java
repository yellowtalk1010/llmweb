package vision.sast.rules.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vision.sast.rules.RulesApplication;

import java.util.HashMap;
import java.util.Map;

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

}
