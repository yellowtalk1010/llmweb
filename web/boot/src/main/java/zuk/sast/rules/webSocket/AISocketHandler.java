package zuk.sast.rules.webSocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class AISocketHandler extends TextWebSocketHandler {

    public static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    public static Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    static {
        zuk.Client.init();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessionMap.put(session.getId(), session);
        sessionMap.entrySet().stream().filter(entry->!entry.getValue().isOpen()).forEach(entry->{
            sessionMap.remove(entry.getKey());
        });
        log.info("连接总数:" + sessionMap.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        try {
            String messageStr = message.getPayload();
            log.info("接受到数据:" + messageStr);
            EXECUTOR_SERVICE.execute(new TestThread(messageStr, session));
        }catch (Exception e) {
            session.sendMessage(new TextMessage(e.getMessage()));
        }

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        try {
            if(sessionMap.get(session.getId()).isOpen()){
                sessionMap.get(session.getId()).close();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            sessionMap.remove(session.getId());
            log.info("删除连接总数:" + sessionMap.size());
        }
    }


    /***
     * 测试 markdown
     */
    private class TestThread implements Runnable {

        private String messageStr;
        private WebSocketSession session;

        public TestThread(String messageStr, WebSocketSession session) {
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
                        this.session.sendMessage(new TextMessage(arr[i]+"")); //一个字符一个字符的输出
                        Thread.sleep(50);
                    }
                    this.session.sendMessage(new TextMessage("\n"));

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

}
