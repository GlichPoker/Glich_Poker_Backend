package ch.uzh.ifi.hase.soprafs24.model;

import java.util.List;

public class RoundModel {
    private final Player player;
    private final long potSize;
    private final long roundBet;
    private final int playersTurn;
    private final long playersTurnId;
    private final long startPlayerId;
    private final int startPlayer;
    private final GameSettings gameSettings;
    private final List<Card> communityCards;
    private final List<PlayerModel> otherPlayers;

    public RoundModel(Round round, long userId) {
        this.player = round.getPlayers()
                .stream()
                .filter(x -> x.getUserId() == userId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(" RoundModel: No player found with userId=" + userId));
        this.potSize = round.potSize;
        this.roundBet = round.roundBet;
        this.playersTurn = round.getPlayersTurn();
        this.playersTurnId = round.getPlayers().get(round.getPlayersTurn()).getUserId();
        this.startPlayer = round.getStartPlayer();
        this.startPlayerId = round.getPlayers().get(round.getStartPlayer()).getUserId();
        this.gameSettings = round.getGameSettings();
        this.communityCards = round.communityCards;
        this.otherPlayers = round.getPlayerModelsOfOtherParticipants(userId);
    }

    public Player getPlayer() {
        return player;
    }

    public long getPotSize() {
        return potSize;
    }

    public long getRoundBet() {
        return roundBet;
    }

    public int getPlayersTurn() {
        return playersTurn;
    }

    public long getPlayersTurnId() {
        return playersTurnId;
    }

    public int getStartPlayer() {
        return startPlayer;
    }

    public long getStartPlayerId() {
        return startPlayerId;
    }

    public GameSettings getGameSettings() {
        return gameSettings;
    }

    public List<Card> getCommunityCards() {
        return communityCards;
    }

    public List<PlayerModel> getOtherPlayers() {
        return otherPlayers;
    }
}
