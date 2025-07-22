package vision.sast.rules.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vision.sast.rules.IssueDatabase;
import vision.sast.rules.dto.IssueDto;

import java.util.*;

@RestController
public class RuleController {

    @GetMapping("rules_list")
    public List<Map<String, Object>> rules_list(){
        List<Map<String, Object>> list = new ArrayList<>();
        IssueDatabase.getVtidList().stream().forEach(vtid->{
            IssueDto dto = IssueDatabase.vtidIssueMap.get(vtid);
            Long size = IssueDatabase.vtidIssueCountMap.get(vtid);
            Map<String, Object> map = new HashMap<>();
            map.put("vtid", vtid);
            map.put("rule", dto.getRule());
            map.put("defectLevel", dto.getDefectLevel());
            map.put("size", size);
            map.put("ruleDesc", dto.getRuleDesc());
            list.add(map);
        });
        return list;
    }


    @GetMapping("rule_vtid")
    public Map<String, Object> rule_vtid(String vtid){

        IssueDto dto = IssueDatabase.vtidIssueMap.get(vtid);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("defectLevel", dto.getDefectLevel());
        responseMap.put("ruleDesc", dto.getRuleDesc());

        List<Map<String, Object>> mapList = new ArrayList<>();
        IssueDatabase.vtidFilesMap.get(vtid).stream().forEach(file->{
            int size = IssueDatabase.queryIssueList(vtid, file).size();
            Map<String, Object> map = new HashMap<>();
            map.put("vtid", vtid);
            map.put("file", file);
            map.put("size", size);
            mapList.add(map);
        });

        responseMap.put("list", mapList);

        return responseMap;
    }

    @GetMapping("llm_rules")
    public String rules(){
        StringBuilder stringBuilder = new StringBuilder();
        IssueDatabase.getVtidList().stream().map(vtid->{
            IssueDto dto = IssueDatabase.vtidIssueMap.get(vtid);
            Long size = IssueDatabase.vtidIssueCountMap.get(vtid);
            String str = "<a href='llm_rule?vtid="+vtid+"'>"+vtid+"</a> &nbsp;&nbsp;&nbsp;" + dto.getDefectLevel() + "-" + size + "&nbsp;/&nbsp;" + dto.getRuleDesc();
            return str + "<br>";
        }).forEach(stringBuilder::append);
        return stringBuilder.toString();
    }

    @GetMapping("llm_rule")
    public String rule(String vtid){

        IssueDto dto = IssueDatabase.vtidIssueMap.get(vtid);
        StringBuilder stringBuilder = new StringBuilder(dto.getDefectLevel() + "/" + dto.getRuleDesc() + "<br>");
        IssueDatabase.vtidFilesMap.get(vtid).stream().map(file->{
            int size = IssueDatabase.queryIssueList(vtid, file).size();
            String str = "<a href='llm_sourcecode?vtid=" + vtid + "&file=" + file + "'>" + file + "</a> &nbsp;&nbsp;&nbsp;" + size;
            return str + "<br>";
        }).forEach(stringBuilder::append);
        return stringBuilder.toString();
    }


}
