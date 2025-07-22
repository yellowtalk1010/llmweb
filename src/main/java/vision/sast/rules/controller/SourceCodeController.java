package vision.sast.rules.controller;


import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vision.sast.rules.IssueDatabase;
import vision.sast.rules.dto.IssueDto;
import vision.sast.rules.utils.SourceCodeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SourceCodeController {

    @GetMapping("llm_sourcecode")
    public synchronized String sourceCode(String vtid, String file, Integer line) {
        if (file != null) {
            try {
                List<IssueDto> issueDtos = new ArrayList<>();
                if(vtid!=null){
                    issueDtos = IssueDatabase.queryIssueList(vtid, file);
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
                List<IssueDto> issueDtos = new ArrayList<>();
                if(vtid.equals("FunctionModule")){
                    //函数建模数据
                    issueDtos = FunctionModuleController.MAP.stream().filter(e->{
                        return e.get("vtId")!=null && e.get("vtId").equals(vtid) && e.get("filePath")!=null && e.get("filePath").equals(file);
                    }).map(e->{
                        IssueDto issueDto = JSONObject.parseObject(JSONObject.toJSONString(e), IssueDto.class);
                        issueDto.setLine(Integer.valueOf(String.valueOf(e.get("line"))));
//                        issueDto.setFilePath(e.get("filePath"));
//                        issueDto.setName(e.get("name"));
//                        issueDto.setTraces(new ArrayList<>());
                        issueDto.setData(e.get("functionModuleInputOutputDto"));
                        return issueDto;
                    }).toList();
                }
                else {
                    issueDtos = IssueDatabase.queryIssueList(vtid, file);
                }

                Pair<List<String>, List<IssueDto>> pair = SourceCodeUtil.show1(file, issueDtos);

                Map<String, Object> map = new HashMap<>();
                map.put("lines", pair.getLeft()); //文件行列表
                map.put("issues", pair.getRight()); //文件对应的issue 列表
                return map;
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if ((vtid == null || StringUtils.isEmpty(vtid)) && file != null) {
            try {
                List<String> lines = SourceCodeUtil.show2(file);
                Map<String, Object> map = new HashMap<>();
                map.put("lines", lines); //文件行列表
                map.put("issues", new ArrayList<>()); //文件对应的issue 列表
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
