package ch.uzh.ifi.hase.soprafs24.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Game implements RoundCompletionListener {
    private final long sessionId;
    private Round round;
    private List<Player> players;
    private GameSettings settings;
    private final long ownerId;
    private final int currentRoundStartPlayer;
    private GameCompletionCallback gameCompletionCallback;

    public interface GameCompletionCallback {
        void onGameComplete(Game game, List<Player> winners);
    }

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

        List<Player> playerList = game.getPlayers() != null
                ? game.getPlayers().stream().map(Player::new).collect(Collectors.toList())
                : new ArrayList<>();

        this.players = playerList;
        this.ownerId = game.getOwner().getId();
        this.currentRoundStartPlayer = game.getStartPlayer();
        this.settings = new GameSettings(game.getSettings());

        if (start) {
            startRound();

            if (this.players.isEmpty() && this.round != null) {
                this.players = this.round.getPlayers();
            }
        }
    }

    public int getCurrentRoundStartPlayer() {
        return currentRoundStartPlayer;
    }

    public void setGameCompletionCallback(GameCompletionCallback gameCompletionCallback) {
        this.gameCompletionCallback = gameCompletionCallback;
    }

    @Override
    public void onRoundComplete(List<Player> winners) {
        if (gameCompletionCallback != null) {
            gameCompletionCallback.onGameComplete(this, winners);
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No GameCompletionCallback set");
        }
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

        for (Player player : players) {
            player.reset();
            player.setIsOnline(true);
        }

        if (players.size() < 2) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "at least two players required to start a round");
        }

        this.round = new Round(players, this.currentRoundStartPlayer, this.settings);
        this.round.setRoundCompletionListener(this);
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
