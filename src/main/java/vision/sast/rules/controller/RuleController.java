package vision.sast.rules.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vision.sast.rules.Database;
import vision.sast.rules.dto.IssueDto;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class RuleController {

    @GetMapping("rules_list")
    public List<Map<String, Object>> rules_list(){
        Database.loadRuleInitList();
        List<Map<String, Object>> list = new ArrayList<>();
        Database.vtidList.stream().forEach(vtid->{
            IssueDto dto = Database.vtidIssueMap.get(vtid);
            if(Database.vtidIssueCountMap.get(vtid)==null){
                long count = Database.ISSUE_RESULT.getResult().stream().filter(r->r.getVtId().equals(vtid)).count();
                Database.vtidIssueCountMap.put(vtid, count);
            }
            long size = Database.vtidIssueCountMap.get(vtid);
            Map<String, Object> map = new HashMap<>();
            map.put("vtid", vtid);
            map.put("defectLevel", dto.getDefectLevel());
            map.put("size", size);
            map.put("ruleDesc", dto.getRuleDesc());
            list.add(map);
        });
        return list;
    }


    @GetMapping("rule_vtid")
    public Map<String, Object> rule_vtid(String vtid){
        if(Database.vtidFilesMap.get(vtid)==null){
            List<String> filepaths = Database.ISSUE_RESULT.getResult().stream().filter(dto->dto.getVtId().equals(vtid))
                    .map(dto->dto.getFilePath()).collect(Collectors.toSet())
                    .stream().toList().stream().sorted().toList();
            Database.vtidFilesMap.put(vtid, filepaths);
        }
        IssueDto dto = Database.vtidIssueMap.get(vtid);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("defectLevel", dto.getDefectLevel());
        responseMap.put("ruleDesc", dto.getRuleDesc());

        List<Map<String, Object>> mapList = new ArrayList<>();
        Database.vtidFilesMap.get(vtid).stream().forEach(file->{
            int size = Database.sourceCodeInit(vtid, file);
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
        Database.loadRuleInitList();
        StringBuilder stringBuilder = new StringBuilder();
        Database.vtidList.stream().map(vtid->{
            IssueDto dto = Database.vtidIssueMap.get(vtid);
            if(Database.vtidIssueCountMap.get(vtid)==null){
                long count = Database.ISSUE_RESULT.getResult().stream().filter(r->r.getVtId().equals(vtid)).count();
                Database.vtidIssueCountMap.put(vtid, count);
            }
            long size = Database.vtidIssueCountMap.get(vtid);
            String str = "<a href='llm_rule?vtid="+vtid+"'>"+vtid+"</a> &nbsp;&nbsp;&nbsp;" + dto.getDefectLevel() + "-" + size + "&nbsp;/&nbsp;" + dto.getRuleDesc();
            return str + "<br>";
        }).forEach(stringBuilder::append);
        return stringBuilder.toString();
    }

    @GetMapping("llm_rule")
    public String rule(String vtid){
        if(Database.vtidFilesMap.get(vtid)==null){
            List<String> filepaths = Database.ISSUE_RESULT.getResult().stream().filter(dto->dto.getVtId().equals(vtid))
                    .map(dto->dto.getFilePath()).collect(Collectors.toSet())
                    .stream().toList().stream().sorted().toList();
            Database.vtidFilesMap.put(vtid, filepaths);
        }
        IssueDto dto = Database.vtidIssueMap.get(vtid);
        StringBuilder stringBuilder = new StringBuilder(dto.getDefectLevel() + "/" + dto.getRuleDesc() + "<br>");
        Database.vtidFilesMap.get(vtid).stream().map(file->{
            int size = Database.sourceCodeInit(vtid, file);
            String str = "<a href='llm_sourcecode?vtid=" + vtid + "&file=" + file + "'>" + file + "</a> &nbsp;&nbsp;&nbsp;" + size;
            return str + "<br>";
        }).forEach(stringBuilder::append);
        return stringBuilder.toString();
    }


}
