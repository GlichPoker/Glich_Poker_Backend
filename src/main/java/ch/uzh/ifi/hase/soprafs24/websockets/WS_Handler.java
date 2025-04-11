package ch.uzh.ifi.hase.soprafs24.websockets;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs24.model.RoundModel;
import ch.uzh.ifi.hase.soprafs24.model.Game;

@Component
public class WS_Handler extends TextWebSocketHandler{
    private final Map<String, CopyOnWriteArraySet<WebSocketSession>> gameSessions = new ConcurrentHashMap<>();
    private final Map<String, CopyOnWriteArraySet<WebSocketSession>> chatSessions = new ConcurrentHashMap<>();

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String clientMessage = message.getPayload();

        if (session.getUri().getPath().equals("/ws/chat")) {
            handleChatMessage(session, clientMessage);
        } else if (session.getUri().getPath().equals("/ws/game")) {
            System.err.println("Game sockets should not send messages.");
        } else {
            System.err.println("Invalid WebSocket path. Closing connection.");
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        Map<String, String> params = splitQuery(query);

        String gameID = params.get("gameID");
        String userID = params.get("userID");
        
        if (gameID != null && userID != null) {
            System.out.println("Game ID: " + gameID + ", \nUser ID: " + userID);
            if (session.getUri().getPath().equals("/ws/game")){
                addSessionToGame(gameID, session);
            } else if (session.getUri().getPath().equals("/ws/chat")){
                addSessionToChat(gameID, session);
            } else {
                System.err.println("Invalid WebSocket path. Closing connection.");
                session.close(CloseStatus.BAD_DATA);
            }
        } else {
            System.err.println("Not all the necessary parameters were provided. Closing connection.");
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        gameSessions.values().forEach(sessions -> sessions.remove(session));
        System.out.println("WebSocket connection closed: " + status);
    }

    public void addSessionToGame(String gameId, WebSocketSession session) {
        gameSessions.computeIfAbsent(gameId, k -> new CopyOnWriteArraySet<WebSocketSession>()).add(session);
    }

    public void addSessionToChat(String chatId, WebSocketSession session) {
        chatSessions.computeIfAbsent(chatId, k -> new CopyOnWriteArraySet<WebSocketSession>()).add(session);
    }

    public void removeSessionFromGame(String gameId, WebSocketSession session) {
        CopyOnWriteArraySet<WebSocketSession> sessions = gameSessions.get(gameId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                gameSessions.remove(gameId);
            }
        }
    }

    public void removeSessionFromChat(String chatId, WebSocketSession session) {
        CopyOnWriteArraySet<WebSocketSession> sessions = chatSessions.get(chatId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                chatSessions.remove(chatId);
            }
        }
    }

    public void sendGameStateToAllInGame(String gameId, String message) {
        CopyOnWriteArraySet<WebSocketSession> sessions = gameSessions.get(gameId);
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastChatToAllOthers(String chatId, String message, WebSocketSession sender) {
        CopyOnWriteArraySet<WebSocketSession> sessions = chatSessions.get(chatId);
        for (WebSocketSession session : sessions) {
            if (!session.equals(sender)) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void closeAllConnections(String gameId, Map<String, CopyOnWriteArraySet<WebSocketSession>> WS_Sessions) {
        CopyOnWriteArraySet<WebSocketSession> sessions = WS_Sessions.get(gameId);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.close(CloseStatus.NORMAL);
                } catch (Exception e) {
                    System.err.println("Error closing session: " + e.getMessage());
                }
            }
        }
        WS_Sessions.remove(gameId);
    }

    private Map<String, String> splitQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null) return map;
        
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2) {
                map.put(pair[0], pair[1]);
            }
        }
        return map;
    }

    public String findChatIdBySession(WebSocketSession session) {
        for (Map.Entry<String, CopyOnWriteArraySet<WebSocketSession>> entry : chatSessions.entrySet()) {
            if (entry.getValue().contains(session)) {
                return entry.getKey(); // Return the game ID
            }
        }
        return null;
    }

    public Void handleChatMessage(WebSocketSession session, String message) {
        String chatId = findChatIdBySession(session);

        broadcastChatToAllOthers(chatId, message, session);

        return null;
    }

    public void sendRoundModelToAll(String gameId, Game game) {
        CopyOnWriteArraySet<WebSocketSession> sessions = gameSessions.get(gameId);
        if (sessions == null || game == null) {
            System.err.println("No sessions found for game " + gameId + " or game is null");
            return;
        }

        for (WebSocketSession session : sessions) {
            try {
                // Extract user ID from the session
                String query = session.getUri().getQuery();
                Map<String, String> params = splitQuery(query);
                String userIdStr = params.get("userID");
                
                if (userIdStr == null) {
                    System.err.println("Session has no userID parameter");
                    continue;
                }
                
                long userId = Long.parseLong(userIdStr);
                
                // Get player-specific RoundModel
                RoundModel roundModel = game.getRoundModel(userId);
                
                // Convert to JSON
                ObjectMapper mapper = new ObjectMapper();
                String roundModelJson = mapper.writeValueAsString(roundModel);
                
                // Send to player
                session.sendMessage(new TextMessage(roundModelJson));
                
            } catch (Exception e) {
                System.err.println("Error sending RoundModel to player: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
