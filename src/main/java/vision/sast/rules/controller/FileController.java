package vision.sast.rules.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vision.sast.rules.Database;
import vision.sast.rules.dto.IssueDto;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class FileController {

    @GetMapping("file_path")
    public List<Map<String, String>> file_path(String f){
        List<IssueDto> ls = Database.fileIssuesMap.get(f);
        File file = new File(f);
        System.out.print(file.getName() + "，" + file.exists() + "，问题总数" + ls.size());

        //数量
        Map<String, List<IssueDto>> vtidGroupMap = ls.stream().collect(Collectors.groupingBy(dto->dto.getVtId()));

        Map<String, String> vtidMap = new HashMap<>();
        List<Map<String, String>> list = new ArrayList<>();
        ls.stream().forEach(dto->{
            int size = 0;
            if(vtidGroupMap.get(dto.getVtId())!=null){
                size = vtidGroupMap.get(dto.getVtId()).size();
            }
            vtidMap.put("file", dto.getFilePath());
            vtidMap.put("size", size + "");
            vtidMap.put("vtid", dto.getVtId());
            vtidMap.put("rule", dto.getRule());
            vtidMap.put("defectLevel", dto.getDefectLevel());
            vtidMap.put("ruleDesc", dto.getRuleDesc());
            list.add(vtidMap);
        });

        return list;
    }

    @GetMapping("file_list")
    public List<Map<String, String>> file_list(){
        System.out.println("文件总数：" + Database.fileList.size());

        List<Map<String, String>> list = new ArrayList<>();
        Database.fileList.stream().forEach(file->{
            Map<String, String> map = new HashMap<>();
            map.put("file", file);
            map.put("size", Database.fileIssuesMap.get(file).size() + "");
            list.add(map);
        });

        return list;
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
