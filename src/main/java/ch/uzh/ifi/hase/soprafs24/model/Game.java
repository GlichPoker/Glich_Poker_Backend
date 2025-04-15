package ch.uzh.ifi.hase.soprafs24.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Game {
    private final long sessionId;
    private Round round;
    private final List<Player> players;
    private GameSettings settings;
    private final long ownerId;
    private final int currentRoundStartPlayer;

    public Game(Player player, GameSettings settings) {
        this.sessionId = System.nanoTime();
        this.players = new ArrayList<>();
        this.players.add(player);
        this.ownerId = player.getUserId();
        this.currentRoundStartPlayer = 0;
        this.settings = settings;
    }

    public Game(ch.uzh.ifi.hase.soprafs24.entity.Game game, boolean start) {
        this.sessionId = game.getSessionId();
        this.players = game.getPlayers().stream()
                .map(Player::new).collect(Collectors.toList());
        this.ownerId = game.getOwner().getId();
        this.currentRoundStartPlayer = game.getStartPlayer();
        this.settings = new GameSettings(game.getSettings());
        if (start)
            startRound();
    }

    public int getCurrentRoundStartPlayer() {
        return currentRoundStartPlayer;
    }

    public GameModel getGameModel(long userId) {
        return new GameModel(this, userId);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public long getSessionId() {
        return sessionId;
    }

    public Round getRound() {
        return round;
    }

    public RoundModel getRoundModel(long userId) {
        return new RoundModel(round, userId);
    }

    public GameSettings getSettings() {
        return settings;
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public Player removePlayer(long userId) {
        return this.players.stream().filter(x -> x.getUserId() == userId).findFirst().orElse(null);
    }

    public Player getPlayer(long userId) {
        return players.stream().filter(x -> x.getUserId() == userId).findFirst().orElse(null);
    }

    public void startRound() {
        List<Player> livePlayers = getOnlinePlayers();
        if (livePlayers.size() < 2)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "at least two players required to start a round");
        this.round = new Round(livePlayers, this.currentRoundStartPlayer, this.settings);
    }

    private List<Player> getOnlinePlayers() {
        return new ArrayList<>(players.stream().filter(Player::isOnline).toList());
    }

    public boolean containsUser(long userId) {
        return this.players.stream().map(Player::getUserId).anyMatch(x -> x == userId);
    }

    public void roundComplete() {
        this.round = null;
    }

    public void joinSession(long userId) {
        Player player = getPlayer(userId);
        if (player == null)
            return;
        player.setIsOnline(true);
    }

    public void adjustSettings(GameSettings gameSettings) {
        this.settings = gameSettings;
    }
}
