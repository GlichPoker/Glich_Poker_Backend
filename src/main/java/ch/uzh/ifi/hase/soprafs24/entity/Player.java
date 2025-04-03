package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;

@Entity
public class Player {
    @Id
    private long userId;

    private String name;
    private double balance;
    private boolean isActive;
    private boolean isOnline;

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "gameId", referencedColumnName = "sessionId")
    private Game game;

    public Player(long userId, String name, double balance) {
        this.userId = userId;
        this.name = name;
        this.balance = balance;
        this.isActive = true;
        this.isOnline = false;
    }

    public Player() {}

    public long getUserId() { return userId; }
    public String getName() { return name; }
    public double getBalance() { return balance; }
    public boolean isActive() { return isActive; }
    public boolean isOnline() { return isOnline; }

    public void setBalance(double balance) { this.balance = balance; }
    public Player setIsOnline(boolean isOnline) { this.isOnline = isOnline; return this; }
    public void increaseBalance(double amount) { this.balance += amount; }

    public void fold() { isActive = false; }

    public void reset() {
        isActive = true;
        this.balance = 0;
    }
}
