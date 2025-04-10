package vision.sast.rules.webSocket.llm;

import java.util.HashMap;
import java.util.Map;

public class LLMPrompt {

    public static Map<String, String> map = new HashMap<String, String>();
    static {
        map.put("CJ2000A_002", "代码中的变量名称是否使用了C/C++的关键字？");
    }

}
