package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.HandRank;
import ch.uzh.ifi.hase.soprafs24.constant.WeatherType;
import ch.uzh.ifi.hase.soprafs24.entity.*;

import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    private GameSettings gameSettings;

    private User owner;
    private Game game;
    private Player player;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");
        List<HandRank> order = new ArrayList<>(Arrays.stream(HandRank.values()).sorted(Comparator.reverseOrder()).toList());

        gameSettings = new GameSettings(1000L, 1L,2L, order, true, WeatherType.CLOUDY, "");


        game = new Game(owner, gameSettings, true);
        player = new Player(owner, 1000, game);
    }

    @Test
    public void getPlayer() {
        when(playerRepository.findByUserId(1L)).thenReturn(Optional.of(player));

        Player p = playerService.getPlayer(1L);

        assertEquals(player, p);
    }

    @Test
    public void getPlayerNotFound() {
        when(playerRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> playerService.getPlayer(1L));
    }

    @Test
    public void savePlayer() {
        playerService.savePlayer(player);
        verify(playerRepository, times(1)).save(any(Player.class));

    }

    @Test
    public void createPlayer() {
        Player createdPlayer = playerService.createPlayer(owner, game.getSettings().getInitialBalance(), game);
        verify(playerRepository, times(1)).save(any(Player.class));
        assertFalse(createdPlayer.isOnline());
        assertEquals(createdPlayer.getBalance(), player.getBalance());
        assertEquals(createdPlayer.getUserId(), player.getUserId());
        assertEquals(createdPlayer.getSessionId(), game.getSessionId());
    }

    @Test
    public void findByGameId() {
        when(playerRepository.findByGameId(game.getSessionId())).thenReturn(new ArrayList<>(){{add(player);}});
        List<Player> players = playerService.findByGameId(game.getSessionId());
        assertEquals(players.size(), 1);
        assertEquals(players.get(0), player);
    }

    @Test
    public void removePlayer() {
        when(playerRepository.findByUserId(1L)).thenReturn(Optional.of(player));
        playerService.removePlayer(owner.getId());
        verify(playerRepository, times(1)).delete(any(Player.class));
    }

    @Test
    public void deleteAllPlayers() {
        List<Player> players = new ArrayList<>(){{add(player);}};
        playerService.deletePlayers(players);
        verify(playerRepository, times(1)).deleteAll(players);
    }
}