package vision.sast.rules.webSocket.llm;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.util.*;

/***
 * {
 * 	"messageId": "aee25250-ea5d-426a-80f6-1f2bef0f84de",
 * 	"messageType": "llm/streamChat",
 * 	"data": {
 * 		"completionOptions": {},
 * 		"title": "DeepSeek Coder (1)",
 * 		"messages": [{
 * 			"role": "system",
 * 			"content": "\u003cimportant_rules\u003e\n  Always include the language and file name in the info string when you write code blocks. If you are editing \"src/main.py\" for example, your code block should start with \u0027```python src/main.py\u0027.\n\u003c/important_rules\u003e"
 * 		        }, {
 * 			"role": "user",
 * 			"content": [{
 * 				"type": "text",
 * 				"text": "\n\n```java SocketTest.java (40-40)\nThread.sleep(200);\n```\n什么意思？"
 *            }]
 *        }, {
 * 			"role": "assistant",
 * 			"content": ""
 *        }]
 *    }
 * }
 */
@Data
public class LLMRequest {
    private String messageId = UUID.randomUUID().toString();
    private String messageType = "llm/streamChat";
    private DataRequest data = null;

    public LLMRequest(String content){
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        map.put("type", "text");
        map.put("text", content);
//        map.put("text", "\n\n```java SocketTest.java (40-40)\nThread.sleep(200);\n```\n什么意思？");
        list.add(map);
        this.data = new DataRequest(list);
    }

    @Data
    public static class DataRequest {
        private Map completionOptions = new HashMap();
//        private String title = "DeepSeek Coder"; //使用deepseek
        private String title = "Autodetect"; //使用本地ollama
        private List<MessagesDto> messages = new ArrayList<>();
        public DataRequest(List<Map<String, String>> content){
            messages.add(new MessagesDto("system", "\u003cimportant_rules\u003e\n  Always include the language and file name in the info string when you write code blocks. If you are editing \"src/main.py\" for example, your code block should start with \u0027```python src/main.py\u0027.\n\u003c/important_rules\u003e"));
            messages.add(new MessagesDto("user", content));
            messages.add(new MessagesDto("assistant", ""));
        }
    }

    @Data
    public static class MessagesDto {
        private String role;
        private Object content;
        public MessagesDto(String role, Object content) {
            this.role = role;
            this.content = content;
        }
    }
}


