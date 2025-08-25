package zuk.sast.rules;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.io.FileUtils;
import zuk.sast.rules.dto.IssueDto;
import zuk.sast.rules.dto.fm.FunctionModuleInputOutputDto;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/***
 * 函数建模数据集
 */
public class DatabaseFunctionModule {

    public static final String FunctionModuleVtid = "FunctionModule";

    private static List<Map<String, String>> MAP = new ArrayList<>();
    private static List<IssueDto> functionModuleIssues = new ArrayList<>();
    private static  List<String> files = new ArrayList<>();

    /***
     * 查询全部需要进行函数建模的文件
     * @return
     */
    public static synchronized List<String> queryAllFiles() {
        if (files == null || files.size() == 0) {
            files = DatabaseIssue.getAllIssue().stream().filter(issueDto -> issueDto.getVtId().equals(FunctionModuleVtid)).map(issueDto -> issueDto.getFilePath()).collect(Collectors.toSet()).stream().toList();
        }
        return files;
    }

    public static IssueDto queryIssueDtoById(String id) {
        List<IssueDto> issueDtos = DatabaseIssue.getAllIssue().stream().filter(issueDto -> issueDto.getId().equals(id)).toList();
        if(issueDtos!=null && issueDtos.size()>0){
            return issueDtos.get(0);
        }
        else {
            return null;
        }
    }


}
