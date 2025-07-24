package vision.sast.rules.webSocket.llm;

import java.util.HashMap;
import java.util.Map;

public class LLMPrompt {

    public static Map<String, String> map = new HashMap<String, String>();
    static {
        map.put("CJ2000A_002", """
                ## Constrains:
                    - 提供准确的评分和改进建议，避免胡编乱造的信息。
                    - 严格遵循工作流。
                ## Workflows:
                    - 分析: 你会以 LLM（大模型）底层的神经网络原理的角度进行思考, 根据以下评分标准对c/c++代码片段进行分析，你十分严格, 有任何不满足神经网络需求的地方指出。
                        + 列出全部变量名称。
                        + 判断变量名称是否与c/c++关键字保留字重名，如果变量名称重名，将其作为问题输出，如果变量名称没有重复，则不输出。
                        
                    - 问题输出(issue output):
                        - 用json格式输出，格式是： {"issues":[{"code":"问题代码"， "line":"代码片段中问题代码行号" }]}
                        - 严格遵循格式，不得在格式中增删改json。
                """);
    }

}
