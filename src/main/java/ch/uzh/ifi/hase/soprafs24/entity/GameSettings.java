package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;

@Entity
public class GameSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long initialBalance;
    private long smallBlind;
    private long bigBlind;

    public GameSettings(long initialBalance, long smallBlind, long bigBlind) {
        this.initialBalance = initialBalance;
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
    }

    public GameSettings() {}

    public long getId() { return id; }
    public long getInitialBalance() { return initialBalance; }
    public long getSmallBlind() { return smallBlind; }
    public long getBigBlind() { return bigBlind; }

    public void setInitialBalance(long initialBalance) { this.initialBalance = initialBalance; }
    public void setSmallBlind(long smallBlind) { this.smallBlind = smallBlind; }
    public void setBigBlind(long bigBlind) { this.bigBlind = bigBlind; }

    public GameSettings toModel() {
        return new GameSettings(initialBalance, smallBlind, bigBlind);
    }
}
