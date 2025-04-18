package ch.uzh.ifi.hase.soprafs24.websockets;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ch.uzh.ifi.hase.soprafs24.constant.Model;
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
    private final String event = "event";

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
                    session.close(CloseStatus.BAD_DATA);
                }
            } else {
                session.close(CloseStatus.BAD_DATA);
            }
        } else {
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        gameSessions.values().forEach(sessions -> sessions.remove(session));
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
            closeAllConnections(groupID, sessionGroup);
            return;
        }
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    // log error
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
                    // log error
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
        String eventString = jsonObject.getString(event);
        Model eventModel = eventString.equals("gameModel") ? Model.GAMEMODEL : eventString.equals("roundModel") ? Model.ROUNDMODEL : Model.SETTINGSMODEL;
        String gameId = jsonObject.getString("gameID");
        long gameIdLong = Long.parseLong(gameId);

        try {
            ch.uzh.ifi.hase.soprafs24.entity.Game gameEntity = gameService.getGameBySessionId(gameIdLong);
            ch.uzh.ifi.hase.soprafs24.model.Game gameModel = new ch.uzh.ifi.hase.soprafs24.model.Game(gameEntity,
                    false);

            sendModelToAll(gameId, gameModel, eventModel);

        } catch (Exception e) {
            // log error
        }

        return null;
    }

    public void sendGameStateToAll(String gameId, String state) {
        CopyOnWriteArraySet<WebSocketSession> sessions = gameSessions.get(gameId);
        if (sessions == null) {
            return;
        }

        try {
            JSONObject stateJson = new JSONObject();
            stateJson.put(event, "GAMESTATECHANGED");  // Changed to uppercase for consistency sideeye to BackendDev
            stateJson.put("state", state);

            String message = stateJson.toString();

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (IOException e) {
                        // log error
                    }
                }
            }
        } catch (Exception e) {
            // log error
        }
    }

    public void sendModelToAll(String gameId, Game game, Model modelType) {
        CopyOnWriteArraySet<WebSocketSession> sessions = gameSessions.get(gameId);
        if (sessions == null || game == null) {
            return;
        }

        for (WebSocketSession session : sessions) {
            try {
                // Extract user ID from the session
                if (session.getUri() == null) {
                    continue;
                }
                String query = session.getUri().getQuery();
                Map<String, String> params = splitQuery(query);
                String userIdStr = params.get("userID");

                if (userIdStr == null) {
                    continue;
                }

                long userId = Long.parseLong(userIdStr);

                Object model = null;
                // Get player-specific RoundModel
                if (modelType == Model.ROUNDMODEL) {
                    if (game.getRoundModel(userId) == null) {
                        continue;
                    }
                    model = game.getRoundModel(userId);

                } else if (modelType == Model.GAMEMODEL) {
                    model = game.getGameModel(userId);
                }

                // Convert to JSON
                ObjectMapper mapper = new ObjectMapper();
                String modelJson = mapper.writeValueAsString(model);

                // add event field to JSON
                JSONObject jsonObject = new JSONObject(modelJson);
                jsonObject.put(event, modelType);
                modelJson = jsonObject.toString();

                // Send to player
                session.sendMessage(new TextMessage(modelJson));

            } catch (Exception e) {
                // log error
            }
        }
    }
}
