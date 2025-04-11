package ch.uzh.ifi.hase.soprafs24.websockets;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket // Enables WebSocket support
public class WS_Config implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Register your handler here
        registry.addHandler(new WS_Handler(), "/ws/game")
                .setAllowedOrigins("*");
        
        registry.addHandler(new WS_Handler(), "/ws/chat")
                .setAllowedOrigins("*");
    }
}
