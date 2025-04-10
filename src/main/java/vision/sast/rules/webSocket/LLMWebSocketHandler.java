package vision.sast.rules.webSocket;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import vision.sast.rules.RulesApplication;
import vision.sast.rules.webSocket.llm.LLMPrompt;
import vision.sast.rules.webSocket.llm.LLMReponse;
import vision.sast.rules.webSocket.llm.LLMRequest;
import vision.sast.rules.webSocket.llm.LLMSocket;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LLMWebSocketHandler extends TextWebSocketHandler {

    //记录websocket的id与WebSockerSession关系
    public static Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("连接建立：" + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String messageStr = message.getPayload();
        System.out.println("收到消息：" + messageStr);
        int index = messageStr.indexOf("##########");
        if(index!=-1){
            String issueId = messageStr.substring(0, index);
            String code = messageStr.substring(index + "##########".length());
            System.out.println(issueId + ":" + code);

            LLMSocket.init();
            sessionMap.put(session.getId(), session);
            //TODO 接受前端发送的代码数据，以及 rule id
            Set<String> set = RulesApplication.ISSUE_RESULT.getResult().stream().filter(issueDto -> issueDto.getId().equals(issueId)).map(dto->dto.getVtId()).collect(Collectors.toSet());
            if(set.size()>0 &&  LLMPrompt.map.get(set.stream().toList().get(0))!=null){
                String prompt = LLMPrompt.map.get(set.stream().toList().get(0));

                String content = "\n\n```c语言代码\n" + code + "\n```\n" + prompt + "用中文回复";
                System.out.println(content);
                //TODO 将 rule id与prompt建立关系，提交给llm
                LLMRequest request = new LLMRequest(content);
                request.setMessageId(session.getId());
                String json = JSONObject.toJSONString(request);
                LLMSocket.writer.write(json + "\r\n");
                LLMSocket.writer.flush();
            }
            else {
                session.sendMessage(new TextMessage("AI暂时支持该规则审计"));
            }


        }

        else {
            // 原样返回消息
            int times = 10;
            for (int i = 0; i < times; i++) {
                session.sendMessage(new TextMessage( "收到：" + messageStr + " : " + i));
            }

        }




    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("连接关闭：" + session.getId());
        try {
            if(sessionMap.get(session.getId()).isOpen()){
                sessionMap.get(session.getId()).close();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            //从记录中移除
            sessionMap.remove(session.getId());
        }
    }

    /***
     * 接受llm发送的数据
     */
    public static void pushMessage(LLMReponse llmReponse){
        try {
            WebSocketSession webSocketSession = LLMWebSocketHandler.sessionMap.get(llmReponse.messageId);

            if (webSocketSession!=null && webSocketSession.isOpen()) {
                webSocketSession.sendMessage(new TextMessage(llmReponse.getData().getContent().getContent()));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
