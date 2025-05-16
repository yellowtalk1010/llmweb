package vision.sast.rules;

import vision.sast.rules.dto.IssueResult;

import java.util.Properties;

public class Database {

    //issue文件路径
    public static String ISSUE_FILEPATH = ""; //统一改为文件上传的方式

    //issue结果保存
    public static IssueResult ISSUE_RESULT = new IssueResult();


    //property中文件加载
    public static Properties PROPERTIES = new Properties();


}
