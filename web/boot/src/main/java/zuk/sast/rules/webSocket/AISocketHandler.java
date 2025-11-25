package zuk.sast.rules.webSocket;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import zuk.Client;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class AISocketHandler extends TextWebSocketHandler {

    public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    public static Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    static {
        zuk.Client.init();

        EXECUTOR_SERVICE.execute(()->{
            while (true) {
                try {
                    //清理断开的websocket连接
                    sessionMap.entrySet().stream().filter(entry->!entry.getValue().isOpen()).forEach(entry->{
                        sessionMap.remove(entry.getKey());
                    });
                    Thread.sleep(500);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        EXECUTOR_SERVICE.execute(()->{
            while (true) {
                try {
                    Thread.sleep(10);
                    if(!Client.queue().isEmpty()){
                        Client.LlmStreamChat llmStreamChat = Client.queue().poll();
                        WebSocketSession session = sessionMap.get(llmStreamChat.getMessageId());
                        if(session!=null && session.isOpen()){
                            session.sendMessage(new TextMessage(llmStreamChat.getData().getContent().getContent()));
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessionMap.put(session.getId(), session);
        //log.info("连接总数:" + sessionMap.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        try {
            String messageStr = message.getPayload();
            log.info("接受到数据:" + messageStr);
            JSONObject jsonObject = JSONObject.parseObject(messageStr);
            String issueId = (String)jsonObject.get("issueId");
            String content = (String)jsonObject.get("content");
            System.out.println("issueId:" + issueId);
            System.out.println("content:" + content);

            //EXECUTOR_SERVICE.execute(new MockDataThread(messageStr, session));  //mock假数据调试
            //new MockDataThread(messageStr, session).run();  //mock假数据调试
            Client.testWrite(session.getId());
        }catch (Exception e) {
            e.printStackTrace();
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


}
