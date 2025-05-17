package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.WeatherType;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private final long sessionId;
    private Round round;
    private List<Player> players;
    private GameSettings settings;
    private final long ownerId;
    private final int currentRoundStartPlayer;
    private final boolean isPublic;
    private final String username;
    private boolean roundRunning;

    public Game(Player player, GameSettings settings) {
        this.sessionId = System.nanoTime();
        this.players = new ArrayList<>();
        this.players.add(player);
        this.ownerId = player.getUserId();
        this.currentRoundStartPlayer = 0;
        this.settings = settings;
        this.isPublic = true;
        this.username = "";
        this.roundRunning = false;
    }

    public Game(ch.uzh.ifi.hase.soprafs24.entity.Game game, boolean start) {
        this.sessionId = game.getSessionId();

        List<Player> playerList = game.getPlayers() != null
                ? game.getPlayers().stream().map(Player::new).toList()
                : new ArrayList<>();

        this.players = playerList;
        this.ownerId = game.getOwner().getId();
        this.currentRoundStartPlayer = game.getStartPlayer();
        this.settings = new GameSettings(game.getSettings());
        this.isPublic = game.isPublic();
        this.username = game.getOwner().getUsername();
        this.roundRunning = game.isRoundRunning();

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

    public GameModel getGameModel(long userId) {
        return new GameModel(this, userId);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public String getUsername() {
        return username;
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
        return new RoundModel(round, userId, settings.weatherType());
    }

    public GameSettings getSettings() {
        return settings;
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public void removePlayer(long userId) {
        players = this.players.stream().filter(x -> x.getUserId() != userId).toList();
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

        int dealCount = settings.weatherType() == WeatherType.SNOWY ? 3 : 2;
        this.round = new Round(players, this.currentRoundStartPlayer % players.size(), this.settings, dealCount);
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

    public boolean isRoundRunning() {
        return roundRunning;
    }
}
