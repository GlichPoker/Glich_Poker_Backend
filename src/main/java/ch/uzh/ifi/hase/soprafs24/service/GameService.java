package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.GameSettings;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerService playerService;
    private final GameSettingsService gameSettingsService;

    @Autowired
    public GameService(GameRepository gameRepository, PlayerService playerService,
            GameSettingsService gameSettingsService) {
        this.gameRepository = gameRepository;
        this.playerService = playerService;
        this.gameSettingsService = gameSettingsService;
    }

    // Create a new game with a player as the owner
    public Game createGame(User owner, long gameSettingsId, boolean isPublic) {
        GameSettings gameSettings = gameSettingsService.getGameSettings(gameSettingsId);
        Game game = new Game(owner, gameSettings, isPublic);
        Game newGame = gameRepository.save(game);
        gameRepository.flush();
        addPlayerToGame(newGame, owner, gameSettings.getInitialBalance());
        return newGame;
    }

    // Get a game by session ID
    public Game getGameBySessionId(long sessionId) {
        Optional<Game> optionalGame = gameRepository.findById(sessionId);
        if (optionalGame.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Game with session id %d not found", sessionId));
        }
        Game game = optionalGame.get();

        List<Player> players = playerService.findByGameId(sessionId);
        game.setPlayers(players);
        return game;
    }

    // Add a player to an existing game
    public boolean addPlayerToGame(Game game, User user, long startBalance) {
        if (game.containsPlayer(user.getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("Player with id %d already exists", user.getId()));
        if (!game.isPublic()) {
            createAndAddPlayerToGame(game, user, startBalance);
        }

        return true;
    }

    public void createAndAddPlayerToGame(Game game, User user, long startBalance) {
        Player player = playerService.createPlayer(user, startBalance, game);
        game.addPlayer(player);
        gameRepository.save(game);
    }

    public boolean handlePlayerJoinOrRejoin(Game game, User user, String password) {

        if(!game.isPublic() && !game.getSettings().getPassword().equals(password))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "password does not match");
        if (!game.containsPlayer(user.getId())) {
            createAndAddPlayerToGame(game, user, game.getSettings().getInitialBalance());
        }

        if (!game.containsPlayer(user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("Player with id %d was not invited to game", user.getId()));
        }
        Player player = game.getPlayer(user.getId());
        player.setIsOnline(true);
        playerService.savePlayer(player);
        return true;
    }

    public boolean removePlayerFromGame(Game game, long userId) {
        if (game.isRoundRunning())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Round is still running");
        game.removePlayer(userId);
        playerService.removePlayer(userId);
        gameRepository.save(game);
        return true;
    }

    public void setPlayerOffline(Game game, long userId) {
        if (game.isRoundRunning())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Round is still running");
        game.getPlayers().stream()
                .filter(x -> x.getUserId() == userId)
                .findFirst().ifPresent(player -> playerService.savePlayer(player.setIsOnline(false)));

        gameRepository.save(game);

    }

    // Start a round in the game
    public void startRound(Game game) {
        game.setRoundRunning(true);
        List<Player> players = game.getPlayers();
        for (Player player : players) {
            player.setIsActive(true);
            playerService.savePlayer(player);
        }
        gameRepository.save(game);
    }

    // Complete a round in the game
    public Game completeRound(ch.uzh.ifi.hase.soprafs24.model.Game game) {
        Game gameEntity = getGameBySessionId(game.getSessionId());
        gameEntity.setRoundRunning(false);
        // TODO shouldn't this be the active players in case some of the palyers are
        // offline?
        gameEntity.setStartPlayer((game.getCurrentRoundStartPlayer() + 1) % game.getPlayers().size());
        List<ch.uzh.ifi.hase.soprafs24.model.Player> newPlayers = game.getPlayers();
        for (ch.uzh.ifi.hase.soprafs24.model.Player activePlayer : newPlayers) {
            Player player = playerService.getPlayer(activePlayer.getUserId());
            player.setBalance(activePlayer.getBalance());
            player.setIsActive(true);
            playerService.savePlayer(player);
        }
        game.roundComplete();
        gameRepository.save(gameEntity);
        return gameEntity;
    }

    public boolean saveSession(Game game) {
        List<Player> players = game.getPlayers();
        for (Player player : players) {
            player.setIsOnline(false);
            player.setIsActive(false);
            playerService.savePlayer(player);
        }
        game.setRoundRunning(false);
        gameRepository.save(game);
        gameRepository.flush();
        return true;
    }

    public void deleteSession(Game game) {
        List<Player> players = game.getPlayers();
        playerService.deletePlayers(players);
        gameRepository.delete(game);
        gameRepository.flush();
        gameSettingsService.deleteSettings(game.getSettings());
    }

    public List<Game> getGamesOwnedByUser(long userId) {
        return gameRepository.getGamesByOwnerId(userId);
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll().stream().toList();
    }
}
