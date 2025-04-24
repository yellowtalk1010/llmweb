package vision.sast.rules.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vision.sast.rules.RulesApplication;
import vision.sast.rules.dto.IssueDto;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
public class RuleController {

    //根据规则vtid统计规则总数
    public static List<String> vtidList;
    //获取规则的基本信息，IssueDto中主要使用规则信息
    public static Map<String, IssueDto> vtidIssueMap = new ConcurrentHashMap<>();
    //获取规则vtid这种规则的总数
    public static Map<String, Long> vtidIssueCountMap = new ConcurrentHashMap<>();

    //规则与文件的关系集合关系
    public static ConcurrentHashMap<String, List<String>> vtidFilesMap = new ConcurrentHashMap<>();

    public synchronized static void loadInitList() {
        if(vtidList ==null){
            Set<String> set = RulesApplication.ISSUE_RESULT.getResult().stream().map(dto->{
                if(vtidIssueMap.get(dto.getVtId())==null){
                    vtidIssueMap.put(dto.getVtId(), dto);
                }
                return dto.getVtId();
            }).collect(Collectors.toSet());
            vtidList = set.stream().toList().stream().sorted().toList();
        }
    }

    @GetMapping("rules")
    public String rules(){
        loadInitList();
        StringBuilder stringBuilder = new StringBuilder();
        vtidList.stream().map(vtid->{
            IssueDto dto = vtidIssueMap.get(vtid);
            if(vtidIssueCountMap.get(vtid)==null){
                long count = RulesApplication.ISSUE_RESULT.getResult().stream().filter(r->r.getVtId().equals(vtid)).count();
                vtidIssueCountMap.put(vtid, count);
            }
            long size = vtidIssueCountMap.get(vtid);
            String str = "<a href='rule?vtid="+vtid+"'>"+vtid+"</a> &nbsp;&nbsp;&nbsp;" + dto.getDefectLevel() + "-" + size + "&nbsp;/&nbsp;" + dto.getRuleDesc();
            return str + "<br>";
        }).forEach(stringBuilder::append);
        return stringBuilder.toString();
    }

    @GetMapping("rules_list")
    public List<Map<String, Object>> rules_list(){
        loadInitList();
        List<Map<String, Object>> list = new ArrayList<>();
        vtidList.stream().forEach(vtid->{
            IssueDto dto = vtidIssueMap.get(vtid);
            if(vtidIssueCountMap.get(vtid)==null){
                long count = RulesApplication.ISSUE_RESULT.getResult().stream().filter(r->r.getVtId().equals(vtid)).count();
                vtidIssueCountMap.put(vtid, count);
            }
            long size = vtidIssueCountMap.get(vtid);
            Map<String, Object> map = new HashMap<>();
            map.put("vtid", vtid);
            map.put("defectLevel", dto.getDefectLevel());
            map.put("size", size);
            map.put("ruleDesc", dto.getRuleDesc());
            list.add(map);
        });
        return list;
    }

    @GetMapping("rule")
    public String rule(String vtid){
        if(vtidFilesMap.get(vtid)==null){
            List<String> filepaths = RulesApplication.ISSUE_RESULT.getResult().stream().filter(dto->dto.getVtId().equals(vtid))
                    .map(dto->dto.getFilePath()).collect(Collectors.toSet())
                    .stream().toList().stream().sorted().toList();
            vtidFilesMap.put(vtid, filepaths);
        }
        IssueDto dto = vtidIssueMap.get(vtid);
        StringBuilder stringBuilder = new StringBuilder(dto.getDefectLevel() + "/" + dto.getRuleDesc() + "<br>");
        vtidFilesMap.get(vtid).stream().map(file->{
            int size = SourceCodeController.init(vtid, file);
            String str = "<a href='sourceCode?vtid=" + vtid + "&file=" + file + "'>" + file + "</a> &nbsp;&nbsp;&nbsp;" + size;
            return str + "<br>";
        }).forEach(stringBuilder::append);
        return stringBuilder.toString();
    }

}
