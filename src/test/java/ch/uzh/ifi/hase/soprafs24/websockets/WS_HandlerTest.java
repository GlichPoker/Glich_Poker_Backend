package ch.uzh.ifi.hase.soprafs24.websockets;

import ch.uzh.ifi.hase.soprafs24.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;

import static org.mockito.Mockito.*;

class WS_HandlerTest {

    @Mock
    private GameService gameService;

    @Mock
    private WebSocketSession session1;

    @Mock
    private WebSocketSession session2;

    // Use @Spy to allow partial mocking if needed, or @InjectMocks if WS_Handler is simple enough
    // For verifying calls to its own methods like addSessionToGame, we'll need a spy.
    @Spy
    @InjectMocks
    private WS_Handler wsHandler;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initializes mocks

        // Common setup for mocked sessions if needed
        when(session1.getUri()).thenReturn(URI.create("ws://localhost/ws/game?gameID=1&userID=100"));
        when(session1.getId()).thenReturn("session1_id");

        when(session2.getUri()).thenReturn(URI.create("ws://localhost/ws/chat?gameID=1"));
        when(session2.getId()).thenReturn("session2_id");
    }

    @Test
    void afterConnectionEstablished_withGamePathAndGameIdAndUserId_callsAddSessionToGame() throws Exception {
        URI uri = URI.create("ws://localhost:8080/ws/game?gameID=123&userID=456");
        when(session1.getUri()).thenReturn(uri);

        wsHandler.afterConnectionEstablished(session1);

        verify(wsHandler).addSessionToGame(eq("123"), eq(session1));
        verify(wsHandler, never()).addSessionToChat(anyString(), any(WebSocketSession.class));
        verify(session1, never()).close(any(CloseStatus.class));
    }

    @Test
    void afterConnectionEstablished_withChatPathAndGameId_callsAddSessionToChat() throws Exception {
        URI uri = URI.create("ws://localhost:8080/ws/chat?gameID=789");
        when(session1.getUri()).thenReturn(uri);

        wsHandler.afterConnectionEstablished(session1);

        verify(wsHandler).addSessionToChat(eq("789"), eq(session1));
        verify(wsHandler, never()).addSessionToGame(anyString(), any(WebSocketSession.class));
        verify(session1, never()).close(any(CloseStatus.class));
    }

    @Test
    void afterConnectionEstablished_withGamePathAndMissingUserId_closesSession() throws Exception {
        URI uri = URI.create("ws://localhost:8080/ws/game?gameID=123"); // No userID
        when(session1.getUri()).thenReturn(uri);

        wsHandler.afterConnectionEstablished(session1);

        verify(session1).close(eq(CloseStatus.BAD_DATA));
        verify(wsHandler, never()).addSessionToGame(anyString(), any(WebSocketSession.class));
        verify(wsHandler, never()).addSessionToChat(anyString(), any(WebSocketSession.class));
    }

    @Test
    void afterConnectionEstablished_withMissingGameId_closesSession() throws Exception {
        URI uri = URI.create("ws://localhost:8080/ws/game?userID=456"); // No gameID
        when(session1.getUri()).thenReturn(uri);

        wsHandler.afterConnectionEstablished(session1);

        verify(session1).close(eq(CloseStatus.BAD_DATA));
        verify(wsHandler, never()).addSessionToGame(anyString(), any(WebSocketSession.class));
        verify(wsHandler, never()).addSessionToChat(anyString(), any(WebSocketSession.class));
    }

    @Test
    void afterConnectionEstablished_withInvalidPath_closesSession() throws Exception {
        URI uri = URI.create("ws://localhost:8080/ws/invalid?gameID=123&userID=456");
        when(session1.getUri()).thenReturn(uri);

        wsHandler.afterConnectionEstablished(session1);

        verify(session1).close(eq(CloseStatus.BAD_DATA));
        verify(wsHandler, never()).addSessionToGame(anyString(), any(WebSocketSession.class));
        verify(wsHandler, never()).addSessionToChat(anyString(), any(WebSocketSession.class));
    }

    @Test
    void handleTextMessage_withGamePath_callsHandleGameMessage() throws Exception {
        URI uri = URI.create("ws://localhost:8080/ws/game?gameID=123&userID=456");
        when(session1.getUri()).thenReturn(uri);
        TextMessage message = new TextMessage("{\"event\":\"TEST_EVENT\",\"gameID\":\"123\"}");

        // Need to ensure the session is "established" for handleTextMessage to work as expected
        // or directly test handleGamemessage if its logic is complex.
        // For this test, we assume the session URI is the primary dispatcher.
        wsHandler.handleTextMessage(session1, message);

        // Verifying the spied method was called.
        // Note: handleGamemessage is public in your WS_Handler, so this works.
        // If it were private, you'd test the public method that calls it.
        verify(wsHandler).handleGamemessage(eq(session1), eq(message.getPayload()));
        verify(wsHandler, never()).handleChatMessage(any(WebSocketSession.class), anyString());
        verify(session1, never()).close(any(CloseStatus.class));
    }

    @Test
    void handleTextMessage_withInvalidPath_closesSession() throws Exception {
        URI uri = URI.create("ws://localhost:8080/ws/invalid");
        when(session1.getUri()).thenReturn(uri);
        TextMessage message = new TextMessage("Test");

        wsHandler.handleTextMessage(session1, message);

        verify(session1).close(eq(CloseStatus.BAD_DATA));
        verify(wsHandler, never()).handleGamemessage(any(WebSocketSession.class), anyString());
        verify(wsHandler, never()).handleChatMessage(any(WebSocketSession.class), anyString());
    }

}