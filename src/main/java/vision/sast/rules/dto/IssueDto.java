package vision.sast.rules.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class IssueDto {
    private String id = UUID.randomUUID().toString();
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
}
