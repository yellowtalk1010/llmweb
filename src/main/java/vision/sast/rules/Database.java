package vision.sast.rules;

import vision.sast.rules.dto.IssueDto;
import vision.sast.rules.dto.IssueResult;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Database {

    //issue文件路径
    public static String ISSUE_FILEPATH = ""; //统一改为文件上传的方式

    //issue结果保存
    public static IssueResult ISSUE_RESULT = new IssueResult();

    //property中文件加载
    public static Properties PROPERTIES = new Properties();


    //文件总数
    public static List<String> fileList = null;
    //文件与issue集合关系
    public static ConcurrentHashMap<String, List<IssueDto>> fileIssuesMap = null;

    public static void fileClear(){
        fileList = null;
        fileIssuesMap = null;
    }

    public synchronized static void loadFileInitList() {
        if(fileList ==null){
            Set<String> set = Database.ISSUE_RESULT.getResult().stream().map(dto->dto.getFilePath()).collect(Collectors.toSet());
            fileList = set.stream().toList().stream().sorted().toList();
        }

        if(fileIssuesMap==null && fileList!=null){
            fileIssuesMap = new ConcurrentHashMap<>();
            fileList.stream().forEach(f->{
                if(fileIssuesMap.get(f)==null){
                    List<IssueDto> dtos = Database.ISSUE_RESULT.getResult().stream().filter(dto->dto.getFilePath().equals(f)).toList();
                    fileIssuesMap.put(f, dtos);
                }
            });
        }

    }


}
