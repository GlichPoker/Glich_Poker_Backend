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

    public GameModel(Game game, long userId) {
        this.sessionId = game.getSessionId();
        // only when round exist, create RoundModel
        if (game.getRound() != null) {
            this.round = game.getRoundModel(userId);
        } else {
            this.round = null;
        }
        int playerIdx = IntStream.range(0, game.getPlayers().size())
                .filter(i -> game.getPlayers().get(i).getUserId() == userId)
                .findFirst()
                .orElse(-1);
        if(playerIdx == -1)throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found");
        List<Player> otherPlayers = new ArrayList<>();
        otherPlayers.addAll(game.getPlayers().subList(playerIdx, game.getPlayers().size()));
        otherPlayers.addAll(game.getPlayers().subList(0, playerIdx));
        this.players = otherPlayers;
        this.settings = game.getSettings();
        this.ownerId = game.getOwnerId();
        this.currentRoundStartPlayer = game.getCurrentRoundStartPlayer();
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
}
