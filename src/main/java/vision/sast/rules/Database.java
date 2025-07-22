package vision.sast.rules;

import com.alibaba.fastjson2.JSONObject;
import lombok.NonNull;
import vision.sast.rules.dto.IssueDto;
import vision.sast.rules.dto.IssueResult;
import vision.sast.rules.utils.SourceCodeUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Database {

    //issue结果保存
    public static IssueResult ISSUE_RESULT = new IssueResult();

    //异步加载文件线程池
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();


    /***
     * 根据文本信息构建一个
     */
    public static void buildIssue(String issuePath) {
        try {
            Database.ruleClear();
            //
            File file = new File(issuePath);
            System.out.println("打开结果路径:" + issuePath + "，" + file.exists());
            FileInputStream fis = new FileInputStream(file);
            //读取结果文件内容
            String content = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            //解析issue结果
            Database.ISSUE_RESULT = JSONObject.parseObject(content, IssueResult.class);
            System.out.println("issue总数:" + Database.ISSUE_RESULT.getResult().size());

            Database.loadFileInitList(); //构建文件关系
            Database.loadRuleInitList(); //构建规则关系
        }catch (Exception exception) {
            exception.printStackTrace();
        }


    }


    /***
     * 文件列表
     */
    public static List<String> fileList = null;

    /***
     *  文件与issue集合关系
     *  key: file
     *  value: 文件中的 issue
     */
    public static ConcurrentHashMap<String, List<IssueDto>> fileIssuesMap = null;

    /***
     * 文件路径与文件的关系
     *  key： file
     *  value： 处理 高亮 后的行信息
     */
    public static Map<String, List<String>> FILE_CONTEXT_MAP = new ConcurrentHashMap<>();

    /***
     * 加载文件信息
     */
    private synchronized static void loadFileInitList() {

        //TODO 从结果中提取文件
        Database.fileList = null;
        Set<String> set = Database.ISSUE_RESULT.getResult().stream().map(dto->dto.getFilePath()).collect(Collectors.toSet());
        Database.fileList = set.stream().toList().stream().sorted().toList();
        System.out.println("完成文件提取，总数量:" + fileList.size());

        //TODO 构建文件与issue关系
        Database.fileIssuesMap = new ConcurrentHashMap<>();
        Database.fileList.stream().forEach(f->{
            if(fileIssuesMap.get(f)==null){
                List<IssueDto> dtos = Database.ISSUE_RESULT.getResult().stream().filter(dto->dto.getFilePath().equals(f)).toList();
                Database.fileIssuesMap.put(f, dtos);
            }
        });
        System.out.println("完成构建文件与issue之间关系");

        Database.FILE_CONTEXT_MAP.clear();
        System.out.println("加载文件内容");
        Database.executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if(Database.fileList.size()>0){
                        Database.ISSUE_RESULT.getResult().stream().map(issueDto -> issueDto.getFilePath()).forEach(issueFile -> {
                            try {
                                if(new File(issueFile).exists()){
                                    SourceCodeUtil.openFile(issueFile);
                                }
                                else {
                                    System.out.println(issueFile + "，文件不存在");
                                }
                            }catch (Exception e) {
                            e.printStackTrace();
                            }
                        });
                    }
                    try {
                        if(Database.fileList.size()>0 && Database.fileList.size()==FILE_CONTEXT_MAP.size()){
                            System.out.println(fileList.size() + "全部完成文件内容缓存加载" + FILE_CONTEXT_MAP.size());
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


    /**
     * 根据规则vtid统计规则总数
     */
    public static java.util.List<String> vtidList = new java.util.ArrayList<>();

    /**
     * 获取规则的基本信息，IssueDto中主要使用规则信息
     * key: vtid;
     * value: checker的具体描述信息
     */
    public static Map<String, IssueDto> vtidIssueMap = new ConcurrentHashMap<>();
    /***
     * 获取规则vtid这种规则的总数，
     * key : vtid，
     * value : 数量
     */
    public static Map<String, Long> vtidIssueCountMap = new ConcurrentHashMap<>();
    /**
     * 规则与文件的关系集合关系
     * key: vtid
     * value: file
     */
    public static ConcurrentHashMap<String, List<String>> vtidFilesMap = new ConcurrentHashMap<>();

    public static void ruleClear(){
        fileAndVtid_issuesMap.clear();
    }

    public synchronized static void loadRuleInitList() {
        vtidList = new java.util.ArrayList<>();
        vtidIssueMap.clear();
        vtidIssueCountMap.clear();
        vtidFilesMap.clear();

        Set<String> set = Database.ISSUE_RESULT.getResult().stream().map(dto->{
            if(vtidIssueMap.get(dto.getVtId())==null){
                vtidIssueMap.put(dto.getVtId(), dto);
            }
            return dto.getVtId();
        }).collect(Collectors.toSet());
        System.out.println("完成构建规则基本信息提取");
        vtidList = set.stream().toList().stream().sorted().toList();
        System.out.println("完成规则提取，规则总数:"  + vtidList.size());


        Database.vtidList.stream().forEach(vtid->{

            //规则违反总数
            long count = Database.ISSUE_RESULT.getResult().stream().filter(r->r.getVtId().equals(vtid)).count();
            vtidIssueCountMap.put(vtid, count);

            //规则违反与文件关系
            List<String> filepaths = Database.ISSUE_RESULT.getResult().stream().filter(dto->dto.getVtId().equals(vtid))
                    .map(dto->dto.getFilePath()).collect(Collectors.toSet())
                    .stream().toList().stream().sorted().toList();
            Database.vtidFilesMap.put(vtid, filepaths);

        });
        System.out.println("完成规则违反总数关系");
        System.out.println("完成规则与文件之间的关系");

    }


    /***
     * 规则 +文件 与 issue 之间的关系
     * key： vtid : file
     * value: 当前文件file中规则vtid的问题总数
     */
    private static Map<String, List<IssueDto>> fileAndVtid_issuesMap = new ConcurrentHashMap<>();

    public static synchronized List<IssueDto> queryIssueList (@NonNull String vtid, @NonNull String file) {
        String key = vtid + ":" + file;

        if(fileAndVtid_issuesMap.get(key)==null){
            List<IssueDto> issueDtos = Database.ISSUE_RESULT.getResult().stream().filter(dto->dto.getFilePath().equals(file) && dto.getVtId().equals(vtid)).toList();
            fileAndVtid_issuesMap.put(key, issueDtos);
        }

        return fileAndVtid_issuesMap.get(key);
    }

}
