package zuk.sast.rules;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import zuk.sast.rules.webSocket.AISocketHandler;
import zuk.sast.rules.webSocket.LLMWebSocketHandler;
import zuk.sast.rules.webSocket.LogSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new LLMWebSocketHandler(), "/ws")
                .addHandler(new LogSocketHandler(), "/log")
                .addHandler(new AISocketHandler(), "/ai")
                .setAllowedOrigins("*"); // 允许跨域
    }
}
