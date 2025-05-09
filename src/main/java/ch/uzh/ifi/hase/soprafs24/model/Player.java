package ch.uzh.ifi.hase.soprafs24.model;

public class Player {
    private final long userId;
    private final String name;
    private double balance;
    private boolean isActive;
    private Card[] hand;
    private long roundBet;
    private boolean isOnline;
    private long totalBet;
    private EvaluationResult evaluationResult;

    public Player(long userId, String name, long balance) {
        this.userId = userId;
        this.name = name;
        this.balance = balance;
        this.isActive = true;
        this.hand = new Card[2];
        this.roundBet = 0;
        this.isOnline = false;
        this.totalBet = 0;
    }

    public Player(ch.uzh.ifi.hase.soprafs24.entity.Player player) {
        this.userId = player.getUserId();
        this.name = player.getName();
        this.balance = player.getBalance();
        this.isActive = player.isActive();
        this.hand = new Card[2];
        this.roundBet = 0;
        this.isOnline = player.isOnline();
        this.totalBet = 0;
    }

    public EvaluationResult getEvaluationResult() {
        return evaluationResult;
    }

    public void setEvaluationResult(EvaluationResult evaluationResult) {
        this.evaluationResult = evaluationResult;
    }

    public long getTotalBet() {
        return totalBet;
    }

    public void setTotalBet(long totalBet) {
        this.totalBet = totalBet;
    }

    public double getBalance() {
        return balance;
    }

    public String getName() {
        return name;
    }

    public long getRoundBet() {
        return roundBet;
    }

    public void setRoundBet(long roundBet) {
        this.roundBet = roundBet;
    }

    public Card[] getHand() {
        return hand;
    }

    public void setHand(Card card, int index) {
        hand[index] = card;
    }

    public void setHand(Card[] cards) {
        hand = cards;
    }

    public void setCard(Card card, int idx) {
        if(idx > -1 && idx < hand.length) return;
        hand[idx] = card;
    }

    public boolean isActive() {
        return isActive;
    }

    public void increaseBalance(double amount) {
        this.balance += amount;
    }

    public long getUserId() {
        return userId;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public void startNewRound() {
        roundBet = 0;
        hand = new Card[2];
        isActive = true;
    }

    public void fold() {
        isActive = false;
    }

    public boolean call(long differnce) {
        if (differnce <= balance && isActive) {
            balance -= differnce;
            roundBet += differnce;
            totalBet += differnce;
        } else {
            isActive = false;
        }
        return isActive;
    }

    protected void reset() {
        roundBet = 0;
        totalBet = 0;
        isActive = true;
        evaluationResult = null;
    }
}
