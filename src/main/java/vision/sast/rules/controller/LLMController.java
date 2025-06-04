package vision.sast.rules.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vision.sast.rules.Database;
import vision.sast.rules.RulesApplication;
import vision.sast.rules.dto.IssueDto;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class LLMController {


    @GetMapping("llm")
    public  String llm(){
        return Database.ISSUE_FILEPATH + "<br>"
                + "issue 总数：" + Database.ISSUE_RESULT.getResult().size() + "<br>"
                + "<a href='llm_files'>文件集</a>"  + "<br>"
                + "<a href='llm_rules'>规则集</a>"  + "<br>"
                + "<a>http://localhost:8080/pages/Rules</a>" + "<br>"
                ;
    }

    @GetMapping("llm_files")
    public String llm_files(){
        System.out.println("文件总数：" + Database.fileList.size());
        StringBuilder stringBuilder = new StringBuilder();
        Database.fileList.stream().map(file->{
            String str = "<a href='llm_file?f="+file+"'>"+file+"</a>&nbsp;&nbsp;&nbsp;" + Database.fileIssuesMap.get(file).size();
            return str + "<br>";
        }).forEach(stringBuilder::append);
        return stringBuilder.toString();
    }

    @GetMapping("llm_file")
    public synchronized String llm_file(String f) {
        List<IssueDto> ls = Database.fileIssuesMap.get(f);
        File file = new File(f);
        System.out.print(file.getName() + "，" + file.exists() + "，问题总数" + ls.size());


        //数量
        Map<String, List<IssueDto>> vtidGroupMap = ls.stream().collect(Collectors.groupingBy(dto->dto.getVtId()));

        Map<String, String> vtidMap = new HashMap<>();
        ls.stream().forEach(dto->{
            int size = 0;
            if(vtidGroupMap.get(dto.getVtId())!=null){
                size = vtidGroupMap.get(dto.getVtId()).size();
            }
            String url = "<a href='llm_sourcecode?vtid=" + dto.getVtId() + "&file=" + f + "'>"+ dto.getVtId() + "</a>&nbsp;&nbsp;&nbsp;" + size
                    + "<br>" + dto.getRule()
                    + "<br>" + dto.getDefectLevel()
                    + "<br>" + dto.getRuleDesc()
                    + "<br>" + "-------------------------------------------------------"
                    + "<br>";
            vtidMap.put(dto.getVtId(), url);
        });

        StringBuilder stringBuilder = new StringBuilder();
        vtidMap.values().forEach(vtid->stringBuilder.append(vtid));

        return f + ", " + ls.size() + "<br>" + stringBuilder.toString();
    }

}
