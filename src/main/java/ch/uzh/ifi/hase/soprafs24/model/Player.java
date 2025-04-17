package ch.uzh.ifi.hase.soprafs24.model;

public class Player {
    private final long userId;
    private final String name;
    private double balance;
    private boolean isActive;
    private Card[] hand;
    private long roundBet;
    private boolean isOnline;

    public Player(long userId, String name, long balance) {
        this.userId = userId;
        this.name = name;
        this.balance = balance;
        this.isActive = true;
        this.hand = new Card[2];
        this.roundBet = 0;
        this.isOnline = false;
    }

    public Player(ch.uzh.ifi.hase.soprafs24.entity.Player player) {
        this.userId = player.getUserId();
        this.name = player.getName();
        this.balance = player.getBalance();
        this.isActive = player.isActive();
        this.hand = new Card[2];
        this.roundBet = 0;
        this.isOnline = player.isOnline();
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

    // can be used for raise, call and check since its called from controller and
    // there we decide on action not here
    public boolean call(long amount) {
        long differnce = amount - roundBet;
        if (differnce <= balance && isActive) {
            balance -= differnce;
            roundBet += differnce;
        } else {
            isActive = false;
        }
        return isActive;
    }

    protected void reset() {
        isActive = true;
        roundBet = 0;
        this.hand = new Card[2];
    }
}
