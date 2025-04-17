package vision.sast.rules.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vision.sast.rules.RulesApplication;
import vision.sast.rules.dto.IssueDto;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
public class RuleController {

    //规则总数
    public static List<String> vtidList;
    public static Map<String, IssueDto> vtidIssueMap = new ConcurrentHashMap<>();
    public static Map<String, Long> vtidIssueCountMap = new ConcurrentHashMap<>();

    //规则与issue集合关系
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
        vtidList.stream().map(e->{
            IssueDto dto = vtidIssueMap.get(e);
            if(vtidIssueCountMap.get(e)==null){
                long count = RulesApplication.ISSUE_RESULT.getResult().stream().filter(r->r.getVtId().equals(e)).count();
                vtidIssueCountMap.put(e, count);
            }
            long size = vtidIssueCountMap.get(e);
            String str = "<a href='rule?vtid="+e+"'>"+e+"</a> &nbsp;&nbsp;&nbsp;" + dto.getDefectLevel() + "-" + size + "&nbsp;/&nbsp;" + dto.getRuleDesc();
            return str + "<br>";
        }).forEach(stringBuilder::append);
        return stringBuilder.toString();
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
