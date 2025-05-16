package vision.sast.rules;

import com.alibaba.fastjson2.JSONObject;
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
    public static String ISSUE_FILEPATH = "";
    //issue结果保存
    public static IssueResult ISSUE_RESULT = new IssueResult();
    //property中文件加载
    public static Properties PROPERTIES = new Properties();

    //文件列表
    public static List<String> fileList = null;
    //文件与issue集合关系
    public static ConcurrentHashMap<String, List<IssueDto>> fileIssuesMap = null;
    /***
     * 根据文本信息构建一个
     * @param content
     */
    public static void buildIssue(String issuePath, String content) {
        Database.ISSUE_FILEPATH = issuePath;
        Database.ISSUE_RESULT = JSONObject.parseObject(content, IssueResult.class);
        RulesApplication.loadProperties();

        System.out.println(Database.ISSUE_RESULT.getResult().size());

        loadFileInitList();


        Database.ruleClear();
        Database.sourceCodeClear();
    }


    /***
     * 加载文件信息
     */
    private synchronized static void loadFileInitList() {

        fileList = null;
        fileIssuesMap = null;

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



    //根据规则vtid统计规则总数
    public static List<String> vtidList;
    //获取规则的基本信息，IssueDto中主要使用规则信息
    public static Map<String, IssueDto> vtidIssueMap = new ConcurrentHashMap<>();
    //获取规则vtid这种规则的总数
    public static Map<String, Long> vtidIssueCountMap = new ConcurrentHashMap<>();
    //规则与文件的关系集合关系
    public static ConcurrentHashMap<String, List<String>> vtidFilesMap = new ConcurrentHashMap<>();

    public static void ruleClear(){
        vtidList = null;
        vtidIssueMap.clear();
        vtidIssueCountMap.clear();
        vtidFilesMap.clear();
    }

    public synchronized static void loadRuleInitList() {
        if(vtidList ==null){
            Set<String> set = Database.ISSUE_RESULT.getResult().stream().map(dto->{
                if(vtidIssueMap.get(dto.getVtId())==null){
                    vtidIssueMap.put(dto.getVtId(), dto);
                }
                return dto.getVtId();
            }).collect(Collectors.toSet());
            vtidList = set.stream().toList().stream().sorted().toList();
        }
    }




    public static Map<String, List<IssueDto>> issuesMap = new ConcurrentHashMap<>();

    public static void sourceCodeClear(){
        issuesMap.clear();
    }

    public static String getKey(String vtid, String file){
        String key = vtid + ":" + file;
        return key;
    }

    public static synchronized int sourceCodeInit(String vtid, String file) {
        String key = getKey(vtid, file);
        if(issuesMap.get(key)==null){
            List<IssueDto> issueDtos = Database.ISSUE_RESULT.getResult().stream().filter(dto->dto.getFilePath().equals(file) && dto.getVtId().equals(vtid)).toList();
            issuesMap.put(key, issueDtos);
        }
        return issuesMap.get(key).size();
    }

}
