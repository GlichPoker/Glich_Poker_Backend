package ch.uzh.ifi.hase.soprafs24.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private final long sessionId;
    private Round round;
    private final List<Player> players;
    private GameSettings settings;
    private final long ownerId;
    private int currentRoundStartPlayer;

    public Game(Player player, GameSettings settings) {
        this.sessionId = System.nanoTime();
        this.players = new ArrayList<>();
        this.players.add(player);
        this.ownerId = player.userId;
        this.currentRoundStartPlayer = 0;
        this.settings = settings;
    }

    public long getOwnerId(){return ownerId;}
    public long getSessionId(){return sessionId;}
    public Round getRound(){return round;}
    public GameSettings getSettings(){return settings;}
    public void addPlayer(Player player){this.players.add(player);}
    public Player getPlayer(long userId){return players.stream().filter(x -> x.userId == userId).findFirst().orElse(null);}

    public void startRound(){
        ArrayList<Player> livePlayers = getOnlinePlayers();
        if(livePlayers.size() < 2) throw new ResponseStatusException(HttpStatus.CONFLICT, "at least two players required to start a round");
        this.round = new Round(livePlayers, this.currentRoundStartPlayer, this.settings);
    }

    private ArrayList<Player> getOnlinePlayers(){
        return new ArrayList<>(players.stream().filter(Player::isOnline).toList());
    }

    public boolean containsUser(long userId){
        return this.players.stream().map(x -> x.userId).anyMatch(x -> x == userId);
    }

    public void roundComplete(){
        this.round = null;
        this.currentRoundStartPlayer = (this.currentRoundStartPlayer + 1) % this.players.size();
    }

    public void joinSession(long userId){
        Player player = getPlayer(userId);
        if(player == null)return;
        player.setIsOnline(true);
    }

    public void adjustSettings(GameSettings gameSettings){
        this.settings = gameSettings;
    }
}
