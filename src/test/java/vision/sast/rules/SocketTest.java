package vision.sast.rules;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import vision.sast.rules.webSocket.llm.LLMReponse;
import vision.sast.rules.webSocket.llm.LLMRequest;
import vision.sast.rules.webSocket.llm.LLMSocket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
 *   }
 * }
 */
public class SocketTest {

    private static ExecutorService pool = Executors.newSingleThreadExecutor();

    public static void main(String[] args) throws Exception{

        LLMRequest request = new LLMRequest("");
        String json = JSONObject.toJSONString(request);

        LLMSocket.init();

        for(String line: getStr().split("\n")){
            line = line.trim();
            if(StringUtils.isNotEmpty(line) && line.startsWith("_handleLine:")){
                line = line.substring("_handleLine:".length());
                System.out.println(line);

                LLMSocket.writer.write(line + "\r\n");
                LLMSocket.writer.flush();
                Thread.sleep(200);
            }
        }

        Thread.sleep(1000000);
    }


    private static String getStr() {
        return """
                _handleLine:{"messageId":"aee25250-ea5d-426a-80f6-1f2bef0f84de","messageType":"llm/streamChat","data":{"completionOptions":{},"title":"DeepSeek Coder (1)","messages":[{"role":"system","content":"\\u003cimportant_rules\\u003e\\n  Always include the language and file name in the info string when you write code blocks. If you are editing \\"src/main.py\\" for example, your code block should start with \\u0027```python src/main.py\\u0027.\\n\\u003c/important_rules\\u003e"},{"role":"user","content":[{"type":"text","text":"\\n\\n```java SocketTest.java (40-40)\\nThread.sleep(200);\\n```\\n什么意思？"}]},{"role":"assistant","content":""}]}}
                
                """;
    }

    /***
     * {
     * 	"messageType": "llm/streamChat",
     * 	"data": {
     * 		"done": false,
     * 		"content": {
     * 			"role": "assistant",
     * 			"content": "。"
     * 		        },
     * 		"status": "success"    * 	},
     * 	"messageId": "aee25250-ea5d-426a-80f6-1f2bef0f84de"
     * }
     *
     * {
     * 	"messageType": "llm/streamChat",
     * 	"data": {
     * 		"done": true,
     * 		"content": {
     * 			"modelTitle": "DeepSeek Coder",
     * 			"prompt": "You are an AI programming assistant, utilizing the DeepSeek Coder model, developed by DeepSeek Company, and your  role is to assist with questions related to computer science. For politically sensitive questions, security and privacy issues, and other non-computer science questions, you will not answer.\n### Instruction:\n<important_rules>\n  Always include the language and file name in the info string when you write code blocks. If you are editing \"src/main.py\" for example, your code block should start with '```python src/main.py'.\n</important_rules>\n\nGive concise responses.\n\n\n\n```java SocketTest.java (40-40)\nThread.sleep(200);\n```\n什么意思？\n### Response:\n",
     * 			"completion": "```java SocketTest.java (40-40)\nThread.sleep(200);\n```\n这行代码的作用是让当前线程暂停执行 **200 毫秒**（0.2秒）。  \n`Thread.sleep()` 是 Java 中用于暂停线程执行的方法，常用于模拟延迟或控制执行节奏。  \n\n参数 `200` 是毫秒数，如果线程被中断（如调用 `interrupt()`），会抛出 `InterruptedException`。",
     * 			"completionOptions": {
     * 				"model": "deepseek-coder",
     * 				"maxTokens": 4096
     * 			            }        * 		},
     * 		"status": "success"
     *    },
     * 	"messageId": "aee25250-ea5d-426a-80f6-1f2bef0f84de"
     * }
     */

}








































