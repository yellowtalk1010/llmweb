package vision.sast.rules;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.io.FileUtils;
import vision.sast.rules.dto.IssueDto;

import java.io.File;
import java.util.*;

public class DatabaseSYSTEM_CONSTRAINTS_01 {

    public static final String VTID = "SYSTEM_CONSTRAINTS_01";
    private static List<IssueDto> SYSTEM_CONSTRAINTS_01_Issues = new ArrayList<>();
    private static  List<String> files = new ArrayList<>();

    /***
     * 语法错误
     * @param SYSTEM_CONSTRAINTS_01_Path
     */
    public static void initFunctionModuleDatabase(String SYSTEM_CONSTRAINTS_01_Path) {
        try {
            SYSTEM_CONSTRAINTS_01_Issues.clear();

            List<String> list = FileUtils.readLines(new File(SYSTEM_CONSTRAINTS_01_Path), "UTF-8");
            Set<String> paths = new HashSet<>();
            list.forEach(jsonline->{
                Map<String, String> map = JSON.parseObject(jsonline, Map.class);

                IssueDto issueDto = JSONObject.parseObject(JSONObject.toJSONString(map), IssueDto.class);

                SYSTEM_CONSTRAINTS_01_Issues.add(issueDto);

                paths.add(issueDto.getFilePath());
            });

            files.addAll(paths);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
