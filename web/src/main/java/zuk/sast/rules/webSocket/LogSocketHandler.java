package zuk.sast.rules.webSocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class LogSocketHandler extends TextWebSocketHandler {

    //记录websocket的id与WebSockerSession关系
    //public static Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
    public static WebSocketSession webSocketSession = null;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("日志连接建立：" + session.getId());

        //sessionMap.put(session.getId(), session);
        webSocketSession = session;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String messageStr = message.getPayload();
        log.info("日志收到消息：" + messageStr);

        try {
            Map<String,String> map = JSONObject.parseObject(messageStr, Map.class);
            log.info("收到消息map：" + JSONObject.toJSONString(map));
            map.put("sessionId", session.getId());

            String issueId = map.get("issueId");
            String content = map.get("content");

            //sessionMap.put(session.getId(), session);

            Map<String,String> map1 = new HashMap<>();
            map1.put("issueId", issueId);
            map1.put("content", content);
            session.sendMessage(new TextMessage("格式：" + JSONObject.toJSONString(map1)));

        }catch (Exception e) {
            Map<String,String> map = new HashMap<>();
            map.put("issueId", "issueId");
            map.put("content", "内容");
            session.sendMessage(new TextMessage(e.getMessage() + "，格式：" + JSONObject.toJSONString(map)));
        }


    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("日志连接关闭：" + session.getId());
        try {
//            if(sessionMap.get(session.getId()).isOpen()){
//                sessionMap.get(session.getId()).close();
//            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            //从记录中移除
//            sessionMap.remove(session.getId());
            webSocketSession = null;
        }
    }

    /***
     * 接受llm发送的数据
     */
    public static void pushMessage(String log, String type){
        try {


                if (webSocketSession!=null && webSocketSession.isOpen()) {
                    Map<String, String> map = new HashMap<>();
                    map.put("log", log);
                    map.put("type", type);
                    webSocketSession.sendMessage(new TextMessage(JSON.toJSONString(map)));
                }

        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

}
