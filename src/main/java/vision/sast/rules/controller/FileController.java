package vision.sast.rules.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vision.sast.rules.Database;
import vision.sast.rules.RulesApplication;
import vision.sast.rules.dto.IssueDto;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
public class FileController {

    //文件总数
    public static List<String> fileList = null;
    //文件与issue集合关系
    public static ConcurrentHashMap<String, List<IssueDto>> fileIssuesMap = null;

    public static void clear(){
        fileList = null;
        fileIssuesMap = null;
    }

    public synchronized static void loadInitList() {
        if(fileList ==null){
            Set<String> set = Database.ISSUE_RESULT.getResult().stream().map(dto->dto.getFilePath()).collect(Collectors.toSet());
            fileList = set.stream().toList().stream().sorted().toList();
        }

        if(fileIssuesMap==null && fileList!=null){
            fileIssuesMap = new ConcurrentHashMap<>();
            fileList.stream().forEach(f->{
                if(fileIssuesMap.get(f)==null){
                    List<IssueDto> dtos = Database.ISSUE_RESULT.getResult().stream().filter(dto->dto.getFilePath().equals(f)).toList();
                    fileIssuesMap.put(f, dtos);
                }
            });
        }

    }

    @GetMapping("file")
    public synchronized String file(String f) {
        loadInitList();
        List<IssueDto> ls = fileIssuesMap.get(f);

        //数量
        Map<String, List<IssueDto>> vtidGroupMap = ls.stream().collect(Collectors.groupingBy(dto->dto.getVtId()));

        Map<String, String> vtidMap = new HashMap<>();
        ls.stream().forEach(dto->{
            int size = 0;
            if(vtidGroupMap.get(dto.getVtId())!=null){
                size = vtidGroupMap.get(dto.getVtId()).size();
            }
            String url = "<a href='sourceCode?vtid=" + dto.getVtId() + "&file=" + f + "'>"+ dto.getVtId() + "</a>&nbsp;&nbsp;&nbsp;" + size
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

    @GetMapping("/llm/files")
    public String files(){
        loadInitList();
        StringBuilder stringBuilder = new StringBuilder();
        fileList.stream().map(file->{
            String str = "<a href='file?f="+file+"'>"+file+"</a>&nbsp;&nbsp;&nbsp;" + fileIssuesMap.get(file).size();
            return str + "<br>";
        }).forEach(stringBuilder::append);
        return stringBuilder.toString();
    }

}
