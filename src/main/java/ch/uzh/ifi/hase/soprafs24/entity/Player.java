package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;

@Entity
@Table(name = "player", uniqueConstraints = @UniqueConstraint(columnNames = {"user_Id", "gameId"}))
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, name = "user_Id")
    private long userId;
    private String name;
    private double balance;
    private boolean isActive;
    private boolean isOnline;

    @ManyToOne
    @JoinColumn(name = "user_Id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "gameId", referencedColumnName = "id")
    private Game game;

    public Player(User user, double balance, Game game) {
        this.name = user.getUsername();
        this.user = user;
        this.userId = user.getId();
        this.balance = balance;
        this.isActive = true;
        this.isOnline = false;
        this.game = game;
    }

    public Player() {}

    public long getSessionId(){return this.game.getSessionId();}
    public long getUserId() { return userId; }
    public String getName() { return name; }
    public double getBalance() { return balance; }
    public boolean isActive() { return isActive; }
    public boolean isOnline() { return isOnline; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }
    public void setBalance(double balance) { this.balance = balance; }
    public Player setIsOnline(boolean isOnline) { this.isOnline = isOnline; return this; }
    public void increaseBalance(double amount) { this.balance += amount; }

    public void fold() { isActive = false; }

    public void reset() {
        isActive = true;
        this.balance = 0;
    }
}
