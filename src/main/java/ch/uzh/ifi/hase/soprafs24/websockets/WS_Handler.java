package ch.uzh.ifi.hase.soprafs24.websockets;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ch.uzh.ifi.hase.soprafs24.constant.Model;

import ch.uzh.ifi.hase.soprafs24.model.BluffModel;
import ch.uzh.ifi.hase.soprafs24.model.WinnerModel;
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
    private final Map<String, Map<Long, String>> weatherVotes = new ConcurrentHashMap<>();

    @Autowired
    public WS_Handler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String clientMessage = message.getPayload();

        URI sessionUri = session.getUri();
        if (sessionUri == null)
            return;

        if (sessionUri.getPath().equals("/ws/chat")) {
            handleChatMessage(session, clientMessage);
        } else if (sessionUri.getPath().equals("/ws/game")) {
            handleGamemessage(session, clientMessage);
        } else {
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        URI sessionUri = session.getUri();
        if (sessionUri == null)
            return;
        String query = sessionUri.getQuery();
        Map<String, String> params = splitQuery(query);

        String gameID = params.get("gameID");
        String userID = params.get("userID");

        if (gameID != null) {
            if (sessionUri.getPath().equals("/ws/chat")) {
                addSessionToChat(gameID, session);
            } else if (sessionUri.getPath().equals("/ws/game")) {
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

        if ("START_WEATHER_VOTE".equals(eventString)) {
            String gameId = jsonObject.getString("gameID");

            JSONObject showVoteButtonMessage = new JSONObject();
            showVoteButtonMessage.put("event", "SHOW_VOTE_MAP_BUTTON");

            broadcastMessage(gameId, showVoteButtonMessage.toString(), gameSessions);
            return null;
        }

        if ("WEATHER_VOTE".equals(eventString)) {
            handleWeatherVote(jsonObject);
            return null;
        }

        Model eventModel = eventString.equals("GAMEMODEL") ? Model.GAMEMODEL
                : eventString.equals("ROUNDMODEL") ? Model.ROUNDMODEL : Model.SETTINGSMODEL;
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

    private void handleWeatherVote(JSONObject jsonObject) {
        String gameId = jsonObject.getString("lobbyId");
        long userId = jsonObject.getLong("userId");
        String weather = jsonObject.getString("weather");

        weatherVotes.putIfAbsent(gameId, new ConcurrentHashMap<>());
        Map<Long, String> votes = weatherVotes.get(gameId);

        if (votes.containsKey(userId)) {
            return;
        }

        votes.put(userId, weather);

        try {
            ch.uzh.ifi.hase.soprafs24.entity.Game gameEntity = gameService.getGameBySessionId(Long.parseLong(gameId));
            int totalPlayers = gameEntity.getPlayers().size();

            if (votes.size() >= totalPlayers) {
                Map<String, Long> voteCounts = votes.values().stream()
                        .collect(Collectors.groupingBy(v -> v, Collectors.counting()));

                String resultWeather = voteCounts.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .get().getKey();

                JSONObject result = new JSONObject();
                result.put("event", "WEATHER_VOTE_RESULT");
                result.put("weatherType", resultWeather);

                broadcastMessage(gameId, result.toString(), gameSessions);

                try {
                    ch.uzh.ifi.hase.soprafs24.model.Game gameModel = new ch.uzh.ifi.hase.soprafs24.model.Game(
                            gameEntity, false);
                    sendModelToAll(gameId, gameModel, Model.GAMEMODEL);
                } catch (Exception e) {
                    System.err.println("Failed to send updated game model: " + e.getMessage());
                }

                weatherVotes.remove(gameId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendGameStateToAll(String gameId, String state) {
        CopyOnWriteArraySet<WebSocketSession> sessions = gameSessions.get(gameId);
        if (sessions == null) {
            return;
        }

        try {
            JSONObject stateJson = new JSONObject();
            stateJson.put(event, "GAMESTATECHANGED"); // Changed to uppercase for consistency sideeye to BackendDev
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

    public void sendRawWinnerModelToPlayer(String gameId, long userId, WinnerModel winnerModel) {
        CopyOnWriteArraySet<WebSocketSession> sessions = gameSessions.get(gameId);
        if (sessions == null) {
            return;
        }

        for (WebSocketSession session : sessions) {
            URI sessionUri = session.getUri();
            if (sessionUri == null) {
                continue;
            }

            String query = sessionUri.getQuery();
            Map<String, String> params = splitQuery(query);
            String sessionUserId = params.get("userID");

            if (!String.valueOf(userId).equals(sessionUserId)) {
                continue;
            }

            try {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(winnerModel);
                JSONObject wrapped = new JSONObject(json);
                wrapped.put("event", Model.WINNINGMODEL.name());

                session.sendMessage(new TextMessage(wrapped.toString()));
            } catch (Exception e) {

            }
        }
    }

    public void sendBluffModelToAll(String gameId, BluffModel model) {
        System.out.println("[DEBUG] sendBluffModelToAll - gameId: " + gameId + ", model: " + model);

        CopyOnWriteArraySet<WebSocketSession> sessions = gameSessions.get(gameId);
        if (sessions == null) {
            System.out.println("[DEBUG] sendBluffModelToAll - No sessions found for gameId: " + gameId);
            return;
        }

        System.out.println("[DEBUG] sendBluffModelToAll - Found " + sessions.size() + " sessions");

        for (WebSocketSession session : sessions) {
            URI sessionUri = session.getUri();
            if (sessionUri == null) {
                System.out.println("[DEBUG] sendBluffModelToAll - Session URI is null, skipping");
                continue;
            }
            try {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(model);
                JSONObject wrapped = new JSONObject(json);
                wrapped.put("event", Model.BLUFFMODEL.name());

                System.out.println("[DEBUG] sendBluffModelToAll - Sending message: " + wrapped.toString());

                session.sendMessage(new TextMessage(wrapped.toString()));

                System.out.println("[DEBUG] sendBluffModelToAll - Message sent successfully");
            } catch (Exception e) {
                System.out.println("[DEBUG] sendBluffModelToAll - Error sending message: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // to send changed weatherType to client
    public void sendGenericToAll(String gameId, Map<String, Object> payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(payload);

            broadcastMessage(gameId, json, gameSessions);
        } catch (IOException e) {
            System.err.println("Failed to send generic message to all: " + e.getMessage());
        }
    }

    public void sendModelToAll(String gameId, Game game, Model modelType) {
        CopyOnWriteArraySet<WebSocketSession> sessions = gameSessions.get(gameId);
        if (sessions == null || game == null) {
            return;
        }

        for (WebSocketSession session : sessions) {
            try {
                URI sessionUri = session.getUri();
                // Extract user ID from the session
                if (sessionUri == null) {
                    continue;
                }
                String query = sessionUri.getQuery();
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
                jsonObject.put(event, modelType.name());
                modelJson = jsonObject.toString();

                // Send to player
                session.sendMessage(new TextMessage(modelJson));

            } catch (Exception e) {
                // log error
            }
        }
    }
}
