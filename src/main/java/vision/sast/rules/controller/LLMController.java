package vision.sast.rules.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vision.sast.rules.Database;
import vision.sast.rules.RulesApplication;

@RestController
public class LLMController {

    public static String baseInfo(){
        return Database.ISSUE_FILEPATH + "<br>"
                + "issue 总数：" + Database.ISSUE_RESULT.getResult().size() + "<br>"
                + "<a href='llm/files'>文件集</a>"  + "<br>"
                + "<a href='llm_rules'>规则集</a>"  + "<br>"
                + "<a>http://localhost:8080/pages/Rules</a>" + "<br>"
                ;
    }


    @GetMapping("llm")
    public  String llm(){
        return baseInfo();
    }

}
