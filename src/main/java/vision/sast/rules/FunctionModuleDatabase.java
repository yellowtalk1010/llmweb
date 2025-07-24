package vision.sast.rules;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.io.FileUtils;
import vision.sast.rules.controller.FunctionModuleController;
import vision.sast.rules.dto.IssueDto;

import java.io.File;
import java.util.*;

/***
 * 函数建模数据集
 */
public class FunctionModuleDatabase {

    public static final String FunctionModuleVtid = "FunctionModule";

    private static List<Map<String, String>> MAP = new ArrayList<>();
    private static List<IssueDto> functionModuleIssues = new ArrayList<>();
    private static  List<String> files = new ArrayList<>();

    /***
     * 查询全部需要进行函数建模的文件
     * @return
     */
    public static List<String> queryAllFiles() {
        return files;
    }

    public static IssueDto queryIssueDtoById(String id) {
        List<IssueDto> issueDtos = functionModuleIssues.stream().filter(issueDto -> issueDto.getId().equals(id)).toList();
        if(issueDtos!=null && issueDtos.size()>0){
            return issueDtos.get(0);
        }
        else {
            return null;
        }
    }

    /***
     * 查询当前文件中需要建模的issue数据
     * @param file
     * @return
     */
    public static List<IssueDto> queryIssuesByFile(String file) {
        return functionModuleIssues.stream().filter(issueDto -> issueDto.getFilePath()!=null && issueDto.getFilePath().equals(file)).toList();
    }

    /***
     * 根据路径，初始化函数建模数据
     * @param funcitonModulePath
     */
    public static void initFunctionModuleDatabase(String funcitonModulePath) {
        try {
            MAP.clear();
            functionModuleIssues.clear();

            List<String> list = FileUtils.readLines(new File(funcitonModulePath), "UTF-8");
            Set<String> paths = new HashSet<>();
            list.forEach(jsonline->{
                Map<String, String> map = JSON.parseObject(jsonline, Map.class);

                IssueDto issueDto = JSONObject.parseObject(JSONObject.toJSONString(map), IssueDto.class);
                issueDto.setLine(Integer.valueOf(String.valueOf(map.get("line"))));
                issueDto.setData(map.get("functionModuleInputOutputDto"));

                functionModuleIssues.add(issueDto);

                paths.add(issueDto.getFilePath());
            });

            files.addAll(paths);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
