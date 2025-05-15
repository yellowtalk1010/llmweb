package vision.sast.rules.controller;


import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vision.sast.rules.RulesApplication;
import vision.sast.rules.dto.IssueDto;
import vision.sast.rules.utils.SourceCodeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class SourceCodeController {

    public static Map<String, List<IssueDto>> issuesMap = new ConcurrentHashMap<>();

    public static void clear(){
        issuesMap.clear();
    }

    private static String getKey(String vtid, String file){
        String key = vtid + ":" + file;
        return key;
    }

    public static synchronized int init(String vtid, String file) {
        String key = getKey(vtid, file);
        if(issuesMap.get(key)==null){
            List<IssueDto> issueDtos = RulesApplication.ISSUE_RESULT.getResult().stream().filter(dto->dto.getFilePath().equals(file) && dto.getVtId().equals(vtid)).toList();
            issuesMap.put(key, issueDtos);
        }
        return issuesMap.get(key).size();
    }

    @GetMapping("llm_sourcecode")
    public synchronized String sourceCode(String vtid, String file, Integer line) {
        if (file != null) {
            try {
                List<IssueDto> issueDtos = new ArrayList<>();
                if(vtid!=null){
                    String key = getKey(vtid, file);
                    issueDtos = issuesMap.get(key);
                }

                String html = SourceCodeUtil.show(file, issueDtos, line);

                html = "<html>" +
                       "<head>" +
                        "<link rel='stylesheet' href='cpp.css'>" +
                        "<link rel='stylesheet' href='float_window.css'>" +
                        "<script src='float_window.js'></script>" +
                        "<script src='https://cdn.jsdelivr.net/npm/marked/marked.min.js'></script>" +
                       "</head>" +
                       "<body>" +
                       "<a href='highLight?file=" + file + "'>源代码</a><br>" +
                       "<pre><code class='language-cpp'>" +
                        html +
                       "</code></pre>" +
                       "</body>" +
                       "</html>";
                return html;
            }catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }
        else {
            return "null";
        }

    }

    @GetMapping("sourceCode_list")
    public synchronized Map<String, Object> sourceCode_list(String vtid, String file) {
        if (vtid != null && file != null) {
            try {
                int size = init(vtid, file);
                String key = getKey(vtid, file);
                List<IssueDto> issueDtos = issuesMap.get(key);
                Pair<List<String>, List<IssueDto>> pair = SourceCodeUtil.show1(file, issueDtos);

                Map<String, Object> map = new HashMap<>();
                map.put("lines", pair.getLeft()); //文件行列表
                map.put("issues", pair.getRight()); //文件对应的issue 列表
                return map;
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("lines", new ArrayList<>());
        map.put("issues", new ArrayList<>());
        return map;

    }

    @GetMapping("otherSourceCode_list")
    public synchronized List<String> otherSourceCode_list(String file) {
        if ( file != null) {
            try {
                List<String> lines = SourceCodeUtil.show2(file);
                return lines;
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

}
