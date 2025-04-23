package ch.uzh.ifi.hase.soprafs24.model;

public class PlayerModel {
    private final long userId;
    private final String name;
    private final double balance;
    private final boolean isActive;
    private final long roundBet;
    private final boolean isOnline;
    private final EvaluationResult evaluationResult;
    public PlayerModel(Player player) {
        this.userId = player.getUserId();
        this.name = player.getName();
        this.balance = player.getBalance();
        this.isActive = player.isActive();
        this.roundBet = player.getRoundBet();
        this.isOnline = player.isOnline();
        this.evaluationResult = player.getEvaluationResult();
    }

    public long getUserId() {return userId;}
    public String getName() {return name;}
    public double getBalance() {return balance;}
    public boolean isActive() {return isActive;}
    public long getRoundBet() {return roundBet;}
    public boolean isOnline() {return isOnline;}

}
