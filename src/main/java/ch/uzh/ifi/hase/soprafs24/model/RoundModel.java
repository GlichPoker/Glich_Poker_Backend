package ch.uzh.ifi.hase.soprafs24.model;

import java.util.List;

public class RoundModel {
    private final Player player;
    private final long potSize;
    private final long roundBet;
    private final int playersTurn;
    private final int startPlayer;
    private final GameSettings gameSettings;
    private final List<Card> communityCards;
    private final List<PlayerModel> otherPlayers;

    public RoundModel(Round round, long userId) {
        this.player = round.getPlayers().stream().filter(x -> x.getUserId() == userId).findFirst().get();
        this.potSize = round.potSize;
        this.roundBet = round.roundBet;
        this.playersTurn = round.getPlayersTurn();
        this.startPlayer = round.getStartPlayer();
        this.gameSettings = round.getGameSettings();
        this.communityCards = round.communityCards;
        this.otherPlayers = round.getPlayerModelsOfOtherParticipants(userId);
    }

    public Player getPlayer() {return player;}
    public long getPotSize(){return potSize;}
    public long getRoundBet(){return roundBet;}
    public int getPlayersTurn(){return playersTurn;}
    public int getStartPlayer(){return startPlayer;}
    public GameSettings getGameSettings(){return gameSettings   ;}
    public List<Card> getCommunityCards(){return communityCards;}
    public List<PlayerModel> getOtherPlayers(){return otherPlayers;}
}
