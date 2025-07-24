package vision.sast.rules;

import java.io.File;

public class RulesApplicationTest {
    public static void main(String[] args) {
        String[] args1 = {"D:/development/github/engine/vision/target/workspace1/projectName/issue.json"};
//        System.out.println(new File(args1[0]).getAbsolutePath());
        RulesApplication.main(args1);
    }
}
