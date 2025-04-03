package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.GameSettings;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerService playerService;
    private final GameSettingsService gameSettingsService;
    private final UserService userService;

    @Autowired
    public GameService(GameRepository gameRepository, PlayerService playerService, GameSettingsService gameSettingsService, UserService userService) {
        this.gameRepository = gameRepository;
        this.playerService = playerService;
        this.gameSettingsService = gameSettingsService;
        this.userService = userService;
    }

    // Create a new game with a player as the owner
    public Game createGame(long ownerId, long gameSettingsId) {
        User owner = userService.getUserById(ownerId);
        GameSettings gameSettings = gameSettingsService.getGameSettings(gameSettingsId);
        Game game = new Game(owner, gameSettings);
        addPlayerToGame(game, ownerId, gameSettings.getInitialBalance());
        Game newGame = gameRepository.save(game);
        gameRepository.flush();
        return newGame;
    }

    // Get a game by session ID
    public Game getGameBySessionId(long sessionId) {
        Optional<Game> game = gameRepository.findBySessionId(sessionId);
        if (game.isEmpty()) {
            throw new IllegalArgumentException("Game not found");
        }
        return game.get();
    }

    // Add a player to an existing game
    public void addPlayerToGame(Game game, long userId, long startBalance) {
        Player activePlayer = game.getPlayer(userId);
        Player player = playerService.createPlayer(userId, activePlayer.getName(), startBalance);
        game.addPlayer(player);
        gameRepository.save(game);
    }


    // Remove a player from the game
    public void removePlayerFromGame(Game game, long userId) {
        game.removePlayer(userId);
        gameRepository.save(game);
    }

    public void setPlayerOffline(Game game, long userId) {
        game.getPlayers().stream()
                .filter(x -> x.getUserId() == userId)
                .findFirst().ifPresent(player -> playerService.savePlayer(player.setIsOnline(false)));

        gameRepository.save(game);

    }

    // Start a round in the game
    public void startRound(Game game) {
        game.setRoundRunning(true);
        gameRepository.save(game);
    }

    // Complete a round in the game
    public void completeRound(ch.uzh.ifi.hase.soprafs24.model.Game game) {
        Game gameEntity = getGameBySessionId(game.getSessionId());
        gameEntity.setRoundRunning(false);
        gameEntity.setStartPlayer((game.getCurrentRoundStartPlayer() + 1) % game.getPlayers().size());
        List<ch.uzh.ifi.hase.soprafs24.model.Player> newPlayers = game.getPlayers();
        for (ch.uzh.ifi.hase.soprafs24.model.Player activePlayer : newPlayers) {
            Player player = playerService.getPlayer(activePlayer.getUserId());
            player.setBalance(activePlayer.getBalance());
            playerService.savePlayer(player);
        }

        gameRepository.save(gameEntity);
    }

    public boolean saveSession(Game game) {
        List<Player> players = game.getPlayers();
        for (Player player : players) {
            player.setIsOnline(false);
            playerService.savePlayer(player);
        }
        game.setRoundRunning(false);
        gameRepository.save(game);
        gameRepository.flush();
        return true;
    }
}
