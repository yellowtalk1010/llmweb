package zuk.sast.rules.dto;

import lombok.Data;

import java.util.UUID;
import java.util.List;

@Data
public class IssueDto {
    private String id;
    private String checkType;
    private String defectLevel;
    private String defectType;
    private String filePath;
    private Integer line;
    private String name;
    private String rule;
    private String ruleDesc;
    private String valid;
    private String vtId;
    private Integer offset;
    private Integer column;
    private String ruleDetailDesc;
    private Integer offsetLength;
    private String issueDesc;
    private List<Trace> traces;

    //其他数据
    private Object data;

}
