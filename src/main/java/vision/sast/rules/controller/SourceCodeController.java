package vision.sast.rules.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vision.sast.rules.RulesApplication;
import vision.sast.rules.controller.html.HTML;
import vision.sast.rules.dto.IssueDto;
import vision.sast.rules.utils.PropertiesKey;
import vision.sast.rules.utils.SourceCodeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class SourceCodeController {

    private static Map<String, List<IssueDto>> issuesMap = new ConcurrentHashMap<>();

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

    @GetMapping("sourceCode")
    public synchronized String sourceCode(String vtid, String file) {
        if (vtid != null && file != null) {
            try {
                int size = init(vtid, file);
                String key = getKey(vtid, file);
                List<IssueDto> issueDtos = issuesMap.get(key);
                String html = SourceCodeUtil.show(file, issueDtos);

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
    public synchronized List<String> sourceCode_list(String vtid, String file) {
        if (vtid != null && file != null) {
            try {
                int size = init(vtid, file);
                String key = getKey(vtid, file);
                List<IssueDto> issueDtos = issuesMap.get(key);
                List<String> line = SourceCodeUtil.show1(file, issueDtos);

//                html = "<html>" +
//                        "<head>" +
//                        "<link rel='stylesheet' href='cpp.css'>" +
//                        "<link rel='stylesheet' href='float_window.css'>" +
//                        "<script src='float_window.js'></script>" +
//                        "<script src='https://cdn.jsdelivr.net/npm/marked/marked.min.js'></script>" +
//                        "</head>" +
//                        "<body>" +
//                        "<a href='highLight?file=" + file + "'>源代码</a><br>" +
//                        "<pre><code class='language-cpp'>" +
//                        html +
//                        "</code></pre>" +
//                        "</body>" +
//                        "</html>";
                return line;
            }catch (Exception e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        }
        else {
            return new ArrayList<>();
        }

    }

}
