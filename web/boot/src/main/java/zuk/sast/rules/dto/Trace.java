package zuk.sast.rules.dto;

import lombok.Data;

@Data
public class Trace {
    private String id;
    private String file;
    private Integer line;
    private String message;
}