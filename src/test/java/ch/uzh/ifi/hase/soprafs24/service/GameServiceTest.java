package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.GameSettings;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private PlayerService playerService;

    @Mock
    private GameSettingsService gameSettingsService;
    @InjectMocks
    private GameService gameService;

    private User owner;
    private GameSettings gameSettings;
    private Game game;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");

        gameSettings = new GameSettings(1000L, 1L,2L);


        game = new Game(owner, gameSettings, true);
    }

    @Test
    public void createGameShouldReturnNewGame() {
        when(gameSettingsService.getGameSettings(1L)).thenReturn(gameSettings);
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        Game createdGame = gameService.createGame(owner, 1L, true);

        assertNotNull(createdGame);
        assertEquals(owner, createdGame.getOwner());
        assertEquals(gameSettings, createdGame.getSettings());
        assertTrue(createdGame.isPublic());
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    public void getGameBySessionIdShouldReturnGame() {
        when(gameRepository.findById(0L)).thenReturn(Optional.of(game));
        when(playerService.findByGameId(0L)).thenReturn(Arrays.asList(new Player()));

        Game fetchedGame = gameService.getGameBySessionId(0L);

        assertNotNull(fetchedGame);
        assertEquals(0L, fetchedGame.getSessionId());
        verify(gameRepository, times(1)).findById(0L);
    }

    @Test
    public void getGameBySessionIdShouldThrowNotFound_WhenGameDoesNotExist() {
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.getGameBySessionId(1L);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getMessage().contains("Game with session id 1 not found"));
    }

    @Test
    public void addPlayerWithPrivateLobbyShouldReturnTrue() {
        Player player = new Player(owner, 1L, game);
        game.setIsPublic(false);
        player.setBalance(1000);
        when(playerService.createPlayer(any(User.class), anyLong(), any(Game.class))).thenReturn(player);
        when(gameRepository.save(any(Game.class))).thenReturn(game);
        gameService.addPlayerToGame(game, owner, 1L);

        verify(gameRepository, times(1)).save(any(Game.class));
        assertTrue(game.getAllPlayers().contains(player));
        assertFalse(game.getPlayer(owner.getId()).isOnline());
    }

    @Test
    public void addPlayerWithPublicLobbyShouldReturnTrue() {
        Player player = new Player(owner, 1L, game);
        player.setBalance(1000);
        when(playerService.createPlayer(any(User.class), anyLong(), any(Game.class))).thenReturn(player);
        when(gameRepository.save(any(Game.class))).thenReturn(game);
        gameService.addPlayerToGame(game, owner, 1L);
        gameService.handlePlayerJoin(game, owner);
        verify(gameRepository, times(1)).save(any(Game.class));
        assertTrue(game.getAllPlayers().contains(player));
        assertTrue(game.getPlayer(owner.getId()).isOnline());
    }

    @Test
    public void removePlayerShouldReturnTrue() {
        Player player = new Player(owner, 1L, game);
        player.setBalance(1000);
        when(playerService.createPlayer(any(User.class), anyLong(), any(Game.class))).thenReturn(player);
        when(gameRepository.save(any(Game.class))).thenReturn(game);
        gameService.addPlayerToGame(game, owner, 1L);
        gameService.removePlayerFromGame(game, owner.getId());
        verify(gameRepository, times(1)).save(any(Game.class));
        assertFalse(game.getAllPlayers().contains(player));
    }

    @Test
    public void setPlayerOfflineShouldReturnTrue() {
        Player player = new Player(owner, 1L, game);
        player.setBalance(1000);
        when(playerService.createPlayer(any(User.class), anyLong(), any(Game.class))).thenReturn(player);
        when(gameRepository.save(any(Game.class))).thenReturn(game);
        gameService.addPlayerToGame(game, owner, 1L);
        gameService.handlePlayerJoin(game, owner);
        assertTrue(game.getAllPlayers().contains(player));
        assertTrue(game.getPlayer(owner.getId()).isOnline());
        gameService.setPlayerOffline(game, owner.getId());
        verify(gameRepository, times(2)).save(any(Game.class));
        assertTrue(game.getAllPlayers().contains(player));
        assertFalse(game.getPlayer(owner.getId()).isOnline());
    }

    @Test
    public void handlePlayerJoinShouldReturnTrue() {
        Player player = new Player(owner, 1L, game);
        player.setBalance(1000);
        when(playerService.createPlayer(any(User.class), anyLong(), any(Game.class))).thenReturn(player);
        when(gameRepository.save(any(Game.class))).thenReturn(game);
        gameService.addPlayerToGame(game, owner, 1L);
        gameService.handlePlayerJoin(game, owner);
        verify(gameRepository, times(1)).save(any(Game.class));
        assertTrue(game.getAllPlayers().contains(player));
        assertTrue(game.getPlayer(owner.getId()).isOnline());
    }

    @Test
    public void startRoundShouldSetRoundRunningTrue() {
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        gameService.startRound(game);

        assertTrue(game.isRoundRunning());
        assertTrue(game.getPlayers().stream().allMatch(Player::isActive));
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    public void saveSessionShouldReturnTrue() {
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        boolean result = gameService.saveSession(game);

        assertTrue(result);
        verify(gameRepository, times(1)).save(game);
    }

    @Test
    public void deleteSessionShouldDeleteGame() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        doNothing().when(playerService).deletePlayers(anyList());
        doNothing().when(gameRepository).delete(game);

        gameService.deleteSession(game);

        verify(playerService, times(1)).deletePlayers(anyList());
        verify(gameRepository, times(1)).delete(game);
    }

    @Test
    public void getGamesOwnedByUserShouldReturnListOfGames() {
        when(gameRepository.getGamesByOwnerId(owner.getId())).thenReturn(Arrays.asList(game));

        List<Game> games = gameService.getGamesOwnedByUser(owner.getId());

        assertNotNull(games);
        assertEquals(1, games.size());
        assertEquals(owner, games.get(0).getOwner());
    }

    @Test
    public void getAllGamesShouldReturnPublicGames() {
        Game publicGame = new Game(owner, gameSettings, true);
        when(gameRepository.findAll()).thenReturn(Arrays.asList(game, publicGame));

        List<Game> publicGames = gameService.getAllGames();

        assertNotNull(publicGames);
        assertEquals(2, publicGames.size());
        assertTrue(publicGames.get(0).isPublic());
    }

    @Test
    public void completeRound() {
        Game game2 = new Game(owner, gameSettings, true);
        game2.setRoundRunning(true);
        Player player = new Player(owner, 1L, game);
        player.setBalance(1000);
        when(playerService.createPlayer(any(User.class), anyLong(), any(Game.class))).thenReturn(player);
        when(gameRepository.save(any(Game.class))).thenReturn(game2);
        when(playerService.getPlayer(anyLong())).thenReturn(player);

        gameService.addPlayerToGame(game2, owner, 1L);
        gameService.handlePlayerJoin(game2, owner);
        ch.uzh.ifi.hase.soprafs24.model.Game gameModel = new ch.uzh.ifi.hase.soprafs24.model.Game(game2, false);

        when(gameRepository.findById(game2.getSessionId())).thenReturn(Optional.of(game2));
        Game savedGame = gameService.completeRound(gameModel);
        verify(gameRepository, times(2)).save(any(Game.class));
        assertNotNull(savedGame);
        assertNull(gameModel.getRound());
        for(ch.uzh.ifi.hase.soprafs24.model.Player p : gameModel.getPlayers()){
            assertTrue(p.isActive());
        }
        assertFalse(savedGame.isRoundRunning());
        assertEquals(0, savedGame.getStartPlayer());
    }
}
