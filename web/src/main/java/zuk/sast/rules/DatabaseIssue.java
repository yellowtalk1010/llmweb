package zuk.sast.rules;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import zuk.sast.rules.controller.ConfigController;
import zuk.sast.rules.controller.mapper.IssueMapper;
import zuk.sast.rules.controller.mapper.entity.IssueEntity;
import zuk.sast.rules.dto.IssueDto;
import zuk.sast.rules.dto.IssueResult;
import zuk.sast.rules.dto.fm.FunctionModuleInputOutputDto;
import zuk.sast.rules.utils.SourceCodeUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/***
 * issue 结果数据集
 */
@Slf4j
public class DatabaseIssue {

    public static final String FunctionModuleVtid = "FunctionModule";  //函数建模vtid

    //项目id
    private static String projectId;
    //异步加载文件线程池
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    //issue结果保存
    private static IssueResult ISSUE_RESULT = new IssueResult();
    //文件列表
    private static List<String> fileList = null;

    /***
     *  文件与issue集合关系
     *  key: file
     *  value: 文件中的 issue
     */
    private static ConcurrentHashMap<String, List<IssueDto>> fileIssuesMap = null;

    /**
     * 根据规则vtid统计规则总数
     */
    private static java.util.List<String> vtidList = new java.util.ArrayList<>();

    /***
     * 文件路径与文件高亮行的关系
     *  key： file
     *  value： 处理 高亮 后的行信息
     */
    private static Map<String, List<String>> FILE_HIGHLIGHT_MAP = new ConcurrentHashMap<>();

    /**
     * 获取规则的基本信息，IssueDto中主要使用规则信息
     * key: vtid;
     * value: checker的具体描述信息
     */
    private static Map<String, IssueDto> vtidIssueMap = new ConcurrentHashMap<>();


    /***
     * 获取规则vtid这种规则的总数，
     * key : vtid，
     * value : 数量
     */
    private static Map<String, Long> vtidIssueCountMap = new ConcurrentHashMap<>();


    /**
     * 规则与文件的关系集合关系
     * key: vtid
     * value: file
     */
    private static ConcurrentHashMap<String, List<String>> vtidFilesMap = new ConcurrentHashMap<>();


    /***
     * 规则 +文件 与 issue 之间的关系
     * key： vtid : file
     * value: 当前文件file中规则vtid的问题总数
     */
    private static Map<String, List<IssueDto>> fileAndVtid_issuesMap = new ConcurrentHashMap<>();

    /***
     * 初始化 issue 结果数据
     */
    public static void initIssues(String proId, IssueMapper issueMapper) {

        if(projectId!=null && projectId.equals(proId)){
            log.info("已本地初始化，issue总数:" + DatabaseIssue.ISSUE_RESULT.getResult().size());
            return;
        }
        try {
            projectId = proId;
            //清理动态生成的数据
            fileAndVtid_issuesMap.clear();
            FILE_HIGHLIGHT_MAP.clear();

            List<IssueEntity> issueEntityList = issueMapper.selectProject(projectId);

            //解析issue结果
            issueEntityList.stream().forEach(issueEntity->{
                try{
                    String line = issueEntity.getContent();
                    Map<String, String> map = JSON.parseObject(line, Map.class);
                    IssueDto issueDto = JSONObject.parseObject(JSONObject.toJSONString(map), IssueDto.class);
                    if (issueDto.getVtId().equals("FunctionModule")) {
                        //如果是函数建模规则，单独处理
                        //issueDto.setLine(Integer.valueOf(String.valueOf(map.get("line"))));
                        FunctionModuleInputOutputDto functionModuleInputOutputDto = JSONObject.parseObject(JSONObject.toJSONString(map.get("functionModuleInputOutputDto")), FunctionModuleInputOutputDto.class);
                        issueDto.setData(functionModuleInputOutputDto);
                    }
                    issueDto.setId(issueEntity.getId());

                    DatabaseIssue.ISSUE_RESULT.getResult().add(issueDto);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            });
            log.info("完成数据本地初始化，issue总数:" + DatabaseIssue.ISSUE_RESULT.getResult().size());

            DatabaseIssue.loadFileInitList(); //构建文件关系
            DatabaseIssue.loadRuleInitList(); //构建规则关系
        }catch (Exception exception) {
            exception.printStackTrace();
        }

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




    /***
     * 加载文件信息
     */
    private synchronized static void loadFileInitList() {

        //TODO 从结果中提取文件
        DatabaseIssue.fileList = null;
        Set<String> set = DatabaseIssue.ISSUE_RESULT.getResult().stream().map(dto->dto.getFilePath()).collect(Collectors.toSet());
        DatabaseIssue.fileList = set.stream().toList().stream().sorted().toList();
        log.info("完成文件提取，总数量:" + fileList.size());

        //TODO 构建文件与issue关系
        DatabaseIssue.fileIssuesMap = new ConcurrentHashMap<>();
        DatabaseIssue.fileList.stream().forEach(f->{
            if(fileIssuesMap.get(f)==null){
                List<IssueDto> dtos = DatabaseIssue.ISSUE_RESULT.getResult().stream().filter(dto->dto.getFilePath().equals(f)).toList();
                DatabaseIssue.fileIssuesMap.put(f, dtos);
            }
        });
        log.info("完成构建文件与issue之间关系");

        log.info("加载文件内容");
        DatabaseIssue.executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if(DatabaseIssue.fileList.size()>0){
                        DatabaseIssue.ISSUE_RESULT.getResult().stream().map(issueDto -> issueDto.getFilePath()).forEach(issueFile -> {
                            try {
                                if(new File(issueFile).exists()){
                                    SourceCodeUtil.openFile(issueFile);
                                }
                                else {
                                    log.info(issueFile + "，文件不存在");
                                }
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    try {
                        if(DatabaseIssue.fileList.size()>0 && DatabaseIssue.fileList.size()== FILE_HIGHLIGHT_MAP.size()){
                            log.info(fileList.size() + "全部完成文件内容缓存加载" + FILE_HIGHLIGHT_MAP.size());
                            break;
                        }
                        Thread.sleep(2000);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }


    public synchronized static void loadRuleInitList() {
        vtidList = new java.util.ArrayList<>();
        vtidIssueMap.clear();
        vtidIssueCountMap.clear();
        vtidFilesMap.clear();

        Set<String> set = DatabaseIssue.ISSUE_RESULT.getResult().stream().map(dto->{
            if(vtidIssueMap.get(dto.getVtId())==null){
                vtidIssueMap.put(dto.getVtId(), dto);
            }
            return dto.getVtId();
        }).collect(Collectors.toSet());
        log.info("完成构建规则基本信息提取");
        vtidList = set.stream().toList().stream().sorted().toList();
        log.info("完成规则提取，规则总数:"  + vtidList.size());


        DatabaseIssue.vtidList.stream().forEach(vtid->{

            //规则违反总数
            long count = DatabaseIssue.ISSUE_RESULT.getResult().stream().filter(r->r.getVtId().equals(vtid)).count();
            vtidIssueCountMap.put(vtid, count);

            //规则违反与文件关系
            List<String> filepaths = DatabaseIssue.ISSUE_RESULT.getResult().stream().filter(dto->dto.getVtId().equals(vtid))
                    .map(dto->dto.getFilePath()).collect(Collectors.toSet())
                    .stream().toList().stream().sorted().toList();
            DatabaseIssue.vtidFilesMap.put(vtid, filepaths);

        });
        log.info("完成规则违反总数关系");
        log.info("完成规则与文件之间的关系");

    }

    /***
     * 获取全部 issue信息
     * @return
     */
    public static List<IssueDto> getAllIssue(){
        return ISSUE_RESULT.getResult().stream().toList();
    }


    /***
     * 查询全部 违反的文件
     * @return
     */
    public static List<String> queryAllFiles() {
        return fileList.stream().toList();
    }

    /***
     * 根据违反 vtid 查询文件
     * @return
     */
    public static List<String> queryAllFiles(String vtid) {
        List<String> files = DatabaseIssue.getAllIssue().stream().filter(issueDto -> issueDto.getVtId().equals(vtid)).map(issueDto -> issueDto.getFilePath()).collect(Collectors.toSet()).stream().toList();
        return files;
    }


    /***
     * 根据文件路径查询当前文件中issue列表
     * @param file
     * @return
     */
    public static List<IssueDto> queryIssuesByFile(String file) {
        return fileIssuesMap.get(file);
    }


    /***
     * 查询文件高亮
     * @param file
     * @return
     */
    public static List<String> queryFileHighLightLines(String file) {
        return FILE_HIGHLIGHT_MAP.get(file);
    }

    /**
     * 插入高亮数据
     * @param file
     * @param lines
     */
    public static void insertFileHighLightLines(String file, List<String> lines) {
        FILE_HIGHLIGHT_MAP.put(file, lines);
    }



    /**
     * 查询全部 vtid
     * */
    public static List<String> queryAllVtidList() { return vtidList; }


    /***
     * 根据vtid查询checker信息
     * @param vtid
     * @return
     */
    public static IssueDto queryCheckerInfo(String vtid) {
        return vtidIssueMap.get(vtid);
    }
    /***
     * 计算 vtid 的issue 数量
     * @param vtid
     * @return
     */
    public static Long queryIssueCount(String vtid) {
        return vtidIssueCountMap.get(vtid);
    }


    /***
     * 查询违反vtid规则的 文件列表
     * @param vtid
     * @return
     */
    public static List<String> queryFilesByVtid(String vtid) {
        return vtidFilesMap.get(vtid);
    }





    public static synchronized List<IssueDto> queryIssueList (@NonNull String vtid, @NonNull String file) {
        String key = vtid + ":" + file;

        if(fileAndVtid_issuesMap.get(key)==null){
            List<IssueDto> issueDtos = DatabaseIssue.ISSUE_RESULT.getResult().stream().filter(dto->dto.getFilePath().equals(file) && dto.getVtId().equals(vtid)).toList();
            fileAndVtid_issuesMap.put(key, issueDtos);
        }

        return fileAndVtid_issuesMap.get(key);
    }

}
