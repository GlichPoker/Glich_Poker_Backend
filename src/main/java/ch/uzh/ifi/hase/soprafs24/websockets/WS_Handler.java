package ch.uzh.ifi.hase.soprafs24.websockets;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.json.JSONObject;


public class WS_Handler extends TextWebSocketHandler{
    private final Map<String, CopyOnWriteArraySet<WebSocketSession>> gameSessions = new ConcurrentHashMap<>();

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String clientMessage = message.getPayload();
        System.out.println("Received message: " + clientMessage);

        //? Echo the message back to all clients in the same game session
        String gameId = findGameIdBySession(session);
        CopyOnWriteArraySet<WebSocketSession> sessions = gameSessions.get(gameId);
        for (WebSocketSession s : sessions) {
            s.sendMessage(new TextMessage("Server received: " + clientMessage));
        }
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        Map<String, String> params = splitQuery(query);

        String gameID = params.get("gameID");
        
        if (gameID != null) {
            System.out.println("Game ID: " + gameID);
            addSessionToGame(gameID, session);
        } else {
            System.err.println("No game ID provided in the query string.");
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

    public void removeSessionFromGame(String gameId, WebSocketSession session) {
        CopyOnWriteArraySet<WebSocketSession> sessions = gameSessions.get(gameId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                gameSessions.remove(gameId);
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

    public void closeAllConnections(String gameId) {
        CopyOnWriteArraySet<WebSocketSession> sessions = gameSessions.get(gameId);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.close(CloseStatus.NORMAL);
                } catch (Exception e) {
                    System.err.println("Error closing session: " + e.getMessage());
                }
            }
        }
        gameSessions.remove(gameId);
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

    public String findGameIdBySession(WebSocketSession session) {
        for (Map.Entry<String, CopyOnWriteArraySet<WebSocketSession>> entry : gameSessions.entrySet()) {
            if (entry.getValue().contains(session)) {
                return entry.getKey(); // Return the game ID
            }
        }
        return null; // Not found
    }

    public void ping(){
        //TODO Implement a method that checks if the connection is still alive
        //TODO For this the client has to implement a ping method that answers with a pong message
    }
}
