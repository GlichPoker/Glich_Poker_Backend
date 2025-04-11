package ch.uzh.ifi.hase.soprafs24.websockets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket // Enables WebSocket support
public class WS_Config implements WebSocketConfigurer {
    @Autowired
    private WS_Handler wsHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Register your handler here
        registry.addHandler(wsHandler, "/ws/game")
                .setAllowedOrigins("*");
        
        registry.addHandler(wsHandler, "/ws/chat")
                .setAllowedOrigins("*");
    }
}
