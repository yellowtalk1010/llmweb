package vision.sast.rules.dto;

import lombok.Data;

import java.util.Map;

/***
 * 函数建模
 */
@Data
public class FunctionModuleInputOutputDto {
//    private String compiler; //编译器
//    private String version; //编译器版本
    private String fileName; //文件名称
    private String fileMD5;  //文件MD5
    private String functionName; //函数名称
    private String functionRawSignature; //函数内容
    private java.util.List<FunctionParamDto> params; //函数参数列表
    private Map<String, String> returnValueMap; //返回值

    @Data
    public static class FunctionParamDto {
        private String paramName; //参数名称
        private String paramRawSignature; //参数内容
        private boolean input;
        private boolean output;
    }

}
