package vision.sast.rules.dto.fm;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FunctionModuleInputOutputDto {

    private String language  = null; //语言

    private String compiler  = null; //编译器
    private String version  = null; //编译器版本

    private String fileName  = null; //文件名称
    private String fileMD5  = null; //文件MD5

    private String funcName  = null; //函数名称
    private Integer funcLine  = null; //函数所在行
    private String funcBase64 = null;//函数内容转base64

    private List<FunctionParamDto> params = new ArrayList<>(); //函数参数列表

    private FunctionReturnValueDto returnValueDto = null; //返回值
}
