package ch.uzh.ifi.hase.soprafs24.model;

import java.util.List;

public class GameModel {
    private final long sessionId;
    private final RoundModel round;
    private final List<Player> players;
    private final GameSettings settings;
    private final long ownerId;
    private final int currentRoundStartPlayer;

    public GameModel(Game game, long userId){
        this.sessionId = game.getSessionId();
        this.round = game.getRoundModel(userId);
        this.players = game.getPlayers();
        this.settings = game.getSettings();
        this.ownerId = game.getOwnerId();
        this.currentRoundStartPlayer = game.getCurrentRoundStartPlayer();
    }

    public long getSessionId() {return sessionId;}
    public RoundModel getRound() {return round;}
    public List<Player> getPlayers() {return players;}
    public GameSettings getSettings() {return settings;}
    public long getOwnerId() {return ownerId;}
    public int getCurrentRoundStartPlayer() {return currentRoundStartPlayer;}
}
