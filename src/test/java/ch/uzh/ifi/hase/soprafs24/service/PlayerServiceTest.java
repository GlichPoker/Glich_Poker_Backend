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

class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;


    private User owner;
    private Game game;
    private Player player;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");
        List<HandRank> order = new ArrayList<>(Arrays.stream(HandRank.values()).sorted(Comparator.reverseOrder()).toList());

        GameSettings gameSettings = new GameSettings(1000L, 1L,2L, order, true, WeatherType.CLOUDY, "");


        game = new Game(owner, gameSettings, true);
        player = new Player(owner, 1000, game);
    }

    @Test
    void getPlayer() {
        when(playerRepository.findByUserAndGameId(1L, game.getSessionId())).thenReturn(Optional.of(player));

        Player p = playerService.getPlayer(1L, game.getSessionId());

        assertEquals(player, p);
    }

    @Test
    void getPlayerNotFound() {
        when(playerRepository.findByUserAndGameId(1L, game.getSessionId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> playerService.getPlayer(1L, game.getSessionId()));
    }

    @Test
    void savePlayer() {
        playerService.savePlayer(player);
        verify(playerRepository, times(1)).save(any(Player.class));

    }

    @Test
    void createPlayer() {
        Player createdPlayer = playerService.createPlayer(owner, game.getSettings().getInitialBalance(), game);
        verify(playerRepository, times(1)).save(any(Player.class));
        assertFalse(createdPlayer.isOnline());
        assertEquals(createdPlayer.getBalance(), player.getBalance());
        assertEquals(createdPlayer.getUserId(), player.getUserId());
        assertEquals(createdPlayer.getSessionId(), game.getSessionId());
    }

    @Test
    void findByGameId() {
        when(playerRepository.findByGameId(game.getSessionId())).thenReturn(new ArrayList<>(){{add(player);}});
        List<Player> players = playerService.findByGameId(game.getSessionId());
        assertEquals(1, players.size());
        assertEquals(player, players.get(0));
    }

    @Test
    void removePlayer() {
        when(playerRepository.findByUserAndGameId(1L, game.getSessionId())).thenReturn(Optional.of(player));
        playerService.removePlayer(owner.getId(), game.getSessionId());
        verify(playerRepository, times(1)).delete(any(Player.class));
    }


    @Test
    void testRemovePlayer(){
        when(playerRepository.findByUserAndGameId(1L, game.getSessionId())).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> playerService.removePlayer(owner.getId(), game.getSessionId()));
    }

    @Test
    void deleteAllPlayers() {
        List<Player> players = new ArrayList<>(){{add(player);}};
        playerService.deletePlayers(players);
        verify(playerRepository, times(1)).deleteAll(players);
    }
}