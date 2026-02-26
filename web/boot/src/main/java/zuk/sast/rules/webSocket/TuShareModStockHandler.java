package zuk.sast.rules.webSocket

import com.alibaba.fastjson2.JSONObject
import lombok.extern.slf4j.Slf4j
import org.springframework.web.socket.{CloseStatus, TextMessage, WebSocketSession}
import org.springframework.web.socket.handler.TextWebSocketHandler
import zuk.LLMClient

import java.util
import java.util.Arrays

@Slf4j
class TuShareModStockHandler extends TextWebSocketHandler {


  /***
   * 建立连接
   */
  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {

  }

  /***
   * 处理websocket连接
   *
   * @param session
   * @param message
   * @throws Exception
   */
  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

  }

  /***
   * 关闭连接
   */
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

  }

}
