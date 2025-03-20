package ch.uzh.ifi.hase.soprafs24.model;

public class GameSettings {
    private final long initialBalance;
    private final long smallBlind;
    private final long bigBlind;
    public long getSmallBlind(){return smallBlind;}
    public long getBigBlind(){return bigBlind;}
    public long getInitialBalance() {return initialBalance;}
    public GameSettings(long initialBalance, long smallBlind, long bigBlind){
        this.initialBalance = initialBalance;
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
    }
}
