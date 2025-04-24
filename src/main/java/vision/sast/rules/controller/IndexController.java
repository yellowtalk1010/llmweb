package vision.sast.rules.controller;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vision.sast.rules.RulesApplication;

@RestController
public class IndexController {

    public static String baseInfo(){
        return RulesApplication.ISSUE_FILEPATH + "<br>"
                + "issue 总数：" + RulesApplication.ISSUE_RESULT.getResult().size() + "<br>"
                + "<a href='files'>文件集</a>"  + "<br>"
                + "<a href='rules'>规则集</a>"  + "<br>"
                ;
    }

//    @GetMapping("")
//    public  String index(){
//        return baseInfo();
//    }

    @GetMapping("llm")
    public  String index1(){
        return baseInfo();
    }

//    @GetMapping("index.html")
//    public  String index2(){
//        return baseInfo();
//    }

}
