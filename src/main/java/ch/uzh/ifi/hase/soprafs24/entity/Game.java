package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Transient
    private List<Player> players;

    @Column(nullable = false)
    private int startPlayer;

    @Column(nullable = false)
    private boolean isPublic;

    @Column(nullable = false)
    private boolean roundRunning;

    @ManyToOne
    @JoinColumn(name = "ownerId", referencedColumnName = "id", updatable = false, insertable = false)
    private User owner;

    @OneToOne
    @JoinColumn(name = "gameSettingId", referencedColumnName = "id")
    private GameSettings settings;

    public Game(User owner, GameSettings settings, boolean isPublic) {
        this.players = new ArrayList<>();
        this.owner = owner;
        this.startPlayer = 0;
        this.settings = settings;
        this.isPublic = isPublic;
    }

    public Game() {}

    public void setPlayers(List<Player> players) {
        this.players = players;
    }
    public long getSessionId() { return id; }
    public User getOwner() { return owner; }
    public List<Player> getPlayers() { return players.stream().filter(Player::isOnline).toList(); }
    public Player getPlayer(long userId){return this.players.stream().filter(player -> player.getUserId() == userId).findFirst().orElse(null);}
    public void addPlayer(Player player) { this.players.add(player); }
    public Player removePlayer(long userId) {
        return this.players.stream().filter(x -> x.getUserId() == userId).findFirst().orElse(null);
    }
    public int getStartPlayer() { return startPlayer; }
    public void setStartPlayer(int startPlayer) { this.startPlayer = startPlayer; }
    public boolean isRoundRunning() { return roundRunning; }
    public void setRoundRunning(boolean roundRunning) { this.roundRunning = roundRunning; }
    public GameSettings getSettings() { return settings; }
    public void setSettings(GameSettings settings) { this.settings = settings; }
    public boolean isPublic() { return isPublic; }
}
