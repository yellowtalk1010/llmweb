package zuk.sast.rules.webSocket;

import org.springframework.web.socket.WebSocketSession;
import zuk.Client;

import java.util.Arrays;

/***
 * MOCK 大模型的 数据
 */
public class MockDataThread implements Runnable {

    private String messageStr;
    private WebSocketSession session;

    public MockDataThread(String messageStr, WebSocketSession session) {
        this.messageStr = messageStr;
        this.session = session;
    }

    @Override
    public void run() {
        String md = getMD();
        md = this.messageStr + "\n" + md;
        Arrays.stream(md.split("\n")).forEach(line->{
            try{
                char arr[] = line.toCharArray();
                for (int i =0; i< arr.length; i++) {
                    Client.ContentData contentData = new Client.ContentData("assistant", arr[i]+"");
                    Client.Data data = new Client.Data(false, contentData, "success");
                    Client.LlmStreamChat llmStreamChat = new Client.LlmStreamChat("llm/streamChat", data, session.getId());
                    Client.queue().put(llmStreamChat);
                }

            }catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    private static final String getMD() {
        return """
                # 一级标题
                ## 二级标题
                ### 三级标题
                                
                这是一个 **加粗** 的文字，这是一个 *斜体*，这是一个 ***加粗+斜体***。 \s
                这是一个 ~~删除线~~。
                                
                ---
                                
                ## 列表示例
                                
                ### 无序列表
                - 苹果
                - 香蕉
                    - 小香蕉
                    - 大香蕉
                - 橘子
                                
                ### 有序列表
                1. 第一项
                2. 第二项
                    1. 子项 A
                    2. 子项 B
                3. 第三项
                                
                ---
                                
                ## 引用
                > 这是一个引用。
                >> 这是嵌套引用。
                                
                ---
                                
                ## 代码示例
                                
                ### 行内代码
                这是 `print("Hello, World!")` 的例子。
                                
                ### 多行代码（Python）
                ```python
                def greet(name):
                    print(f"Hello, {name}!")
                                
                greet("张三")
                                
                """;
    }
}
