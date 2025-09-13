package zuk.sast.rules.controller.mapper.entity;

import lombok.Data;

@Data
public class IssueEntity {
    private String id;
    private String projectId;
    private Long num;
    private String content;
}
