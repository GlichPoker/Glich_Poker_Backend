package ch.uzh.ifi.hase.soprafs24.websockets;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.io.IOException;
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

import ch.uzh.ifi.hase.soprafs24.model.Game;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class WS_Handler extends TextWebSocketHandler {
    private final Map<String, CopyOnWriteArraySet<WebSocketSession>> gameSessions = new ConcurrentHashMap<>();
    private final Map<String, CopyOnWriteArraySet<WebSocketSession>> chatSessions = new ConcurrentHashMap<>();
    private final GameService gameService;

    @Autowired
    public WS_Handler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String clientMessage = message.getPayload();

        if (session.getUri().getPath().equals("/ws/chat")) {
            handleChatMessage(session, clientMessage);
        } else if (session.getUri().getPath().equals("/ws/game")) {
            handleGamemessage(session, clientMessage);
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

        if (gameID != null) {
            if (session.getUri().getPath().equals("/ws/chat")) {
                addSessionToChat(gameID, session);
            } else if (session.getUri().getPath().equals("/ws/game")) {
                if (userID != null) {
                    addSessionToGame(gameID, session);
                } else {
                    System.err.println("User ID is null. Closing connection.");
                    session.close(CloseStatus.BAD_DATA);
                }
            } else {
                System.err.println("Invalid WebSocket path. Closing connection.");
                session.close(CloseStatus.BAD_DATA);
            }
        } else {
            System.err.println("Game ID is null. Closing connection.");
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

    public void broadcastMessage(String groupID, String message,
            Map<String, CopyOnWriteArraySet<WebSocketSession>> sessionGroup) {
        CopyOnWriteArraySet<WebSocketSession> sessions = sessionGroup.get(groupID);
        if (sessions == null) {
            System.err.println("No sessions found for group " + groupID);
            closeAllConnections(groupID, sessionGroup);
            return;
        }
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    System.err.println("Error sending message: " + e.getMessage());
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
        if (query == null)
            return map;

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

        broadcastMessage(chatId, message, chatSessions);

        return null;
    }

    public Void handleGamemessage(WebSocketSession session, String message) {
        JSONObject jsonObject = new JSONObject(message);
        String event = jsonObject.getString("event");
        String gameId = jsonObject.getString("gameID");
        long gameIdLong = Long.parseLong(gameId);

        try {
            ch.uzh.ifi.hase.soprafs24.entity.Game gameEntity = gameService.getGameBySessionId(gameIdLong);
            ch.uzh.ifi.hase.soprafs24.model.Game gameModel = new ch.uzh.ifi.hase.soprafs24.model.Game(gameEntity,
                    false);

            sendModelToAll(gameId, gameModel, event);

        } catch (Exception e) {
            System.err.println("Error retrieving game for WebSocket update: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public void sendGameStateToAll(String gameId, String state) {
        CopyOnWriteArraySet<WebSocketSession> sessions = gameSessions.get(gameId);
        if (sessions == null) {
            System.err.println("No sessions found for game " + gameId);
            return;
        }

        try {
            JSONObject stateJson = new JSONObject();
            stateJson.put("event", "gameStateChanged");
            stateJson.put("state", state);

            String message = stateJson.toString();

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (IOException e) {
                        System.err.println("Error sending game state to player: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error creating game state message: " + e.getMessage());
        }
    }

    public void sendModelToAll(String gameId, Game game, String modelType) {
        CopyOnWriteArraySet<WebSocketSession> sessions = gameSessions.get(gameId);
        if (sessions == null || game == null) {
            System.err.println("No sessions found for game " + gameId + " or game is null");
            return;
        }

        for (WebSocketSession session : sessions) {
            try {
                // Extract user ID from the session
                if (session.getUri() == null) {
                    System.err.println("Session URI is null");
                    continue;
                }
                String query = session.getUri().getQuery();
                Map<String, String> params = splitQuery(query);
                String userIdStr = params.get("userID");

                if (userIdStr == null) {
                    System.err.println("Session has no userID parameter");
                    continue;
                }

                long userId = Long.parseLong(userIdStr);

                Object model = null;
                // Get player-specific RoundModel
                if (modelType.equals("roundModel")) {
                    if (game.getRoundModel(userId) == null) {
                        System.err.println("RoundModel is null for userId: " + userId);
                        continue;
                    }
                    model = game.getRoundModel(userId);

                } else if (modelType.equals("gameModel")) {
                    model = game.getGameModel(userId);
                }

                // Convert to JSON
                ObjectMapper mapper = new ObjectMapper();
                String modelJson = mapper.writeValueAsString(model);

                // add event field to JSON
                JSONObject jsonObject = new JSONObject(modelJson);
                jsonObject.put("event", modelType);
                modelJson = jsonObject.toString();

                // Send to player
                session.sendMessage(new TextMessage(modelJson));

            } catch (Exception e) {
                System.err.println("Error sending RoundModel to player: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
