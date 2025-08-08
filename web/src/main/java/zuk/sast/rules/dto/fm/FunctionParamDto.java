package zuk.sast.rules.dto.fm;

import lombok.Data;

@Data
public class FunctionParamDto {
    private String param;
    private String in_out;
}
