package vision.sast.rules.dto;

import lombok.Data;

import java.util.Map;

/***
 * 函数建模
 */
@Data
public class FunctionModuleInputOutputDto {
    private String compiler; //编译器
    private String version; //编译器版本

    private String fileName; //文件名称
    private String fileMD5;  //文件MD5

    private String funcName; //函数名称
    private Integer funcLine; //函数所在行
    private String funcBase64; //函数内容转base64

    private java.util.List<FunctionParamDto> params; //函数参数列表
    private FunctionReturnValueDto returnValueDto; //返回值

    @Data
    public static class FunctionParamDto {
        private String paramName; //参数名称
        private String paramRawSignature; //参数内容
        private boolean input;
        private boolean output;
    }

    @Data
    public static class FunctionReturnValueDto {
        private Boolean isLiteral;
        private String literalValue;
        private Map<String, String> returnValueMap;
    }

}
