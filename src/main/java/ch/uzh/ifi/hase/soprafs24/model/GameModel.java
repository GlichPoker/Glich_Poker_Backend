package ch.uzh.ifi.hase.soprafs24.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class GameModel {
    private final long sessionId;
    private final RoundModel round;
    private final List<Player> players;
    private final GameSettings settings;
    private final long ownerId;
    private final int currentRoundStartPlayer;
    private final boolean isPublic;
    private final String username;
    private final boolean roundRunning;

    public GameModel(Game game, long userId) {
        this.sessionId = game.getSessionId();
        // only when round exist, create RoundModel
        if (game.getRound() != null) {
            this.round = game.getRoundModel(userId);
        } else {
            this.round = null;
        }

        if (userId > 0) {
            List<Player> p = game.getPlayers();
            int playerIdx = IntStream.range(0, p.size())
                    .filter(i -> p.get(i).getUserId() == userId)
                    .findFirst()
                    .orElse(-1);

            if (playerIdx == -1)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found");

            List<Player> orderedPlayers = new ArrayList<>();
            orderedPlayers.addAll(p.subList(playerIdx, p.size()));
            orderedPlayers.addAll(p.subList(0, playerIdx));
            this.players = orderedPlayers;

        } else {
            this.players = game.getPlayers();
        }
        this.settings = game.getSettings();
        this.ownerId = game.getOwnerId();
        this.currentRoundStartPlayer = game.getCurrentRoundStartPlayer();
        this.isPublic = game.isPublic();
        this.username = game.getUsername();
        this.roundRunning = game.isRoundRunning();
    }

    public long getSessionId() {
        return sessionId;
    }

    public RoundModel getRound() {
        return round;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public GameSettings getSettings() {
        return settings;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public int getCurrentRoundStartPlayer() {
        return currentRoundStartPlayer;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public String getUsername() {
        return username;
    }

    public boolean isRoundRunning() {
        return roundRunning;
    }
}
