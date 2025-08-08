package zuk.sast.rules.dto.fm;

import lombok.Data;

@Data
public class FunctionReturnValueDto {
    private Boolean isLiteral;
    private String literalValue;
}
