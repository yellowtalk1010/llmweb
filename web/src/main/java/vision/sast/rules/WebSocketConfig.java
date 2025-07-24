package vision.sast.rules;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import vision.sast.rules.webSocket.LLMWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new LLMWebSocketHandler(), "/ws")
                .setAllowedOrigins("*"); // 允许跨域
    }
}
