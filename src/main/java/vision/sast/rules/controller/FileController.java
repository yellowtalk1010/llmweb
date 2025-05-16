package vision.sast.rules.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vision.sast.rules.Database;
import vision.sast.rules.dto.IssueDto;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
public class FileController {

    @GetMapping("file")
    public synchronized String file(String f) {
        List<IssueDto> ls = Database.fileIssuesMap.get(f);

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
        StringBuilder stringBuilder = new StringBuilder();
        Database.fileList.stream().map(file->{
            String str = "<a href='file?f="+file+"'>"+file+"</a>&nbsp;&nbsp;&nbsp;" + Database.fileIssuesMap.get(file).size();
            return str + "<br>";
        }).forEach(stringBuilder::append);
        return stringBuilder.toString();
    }

}
