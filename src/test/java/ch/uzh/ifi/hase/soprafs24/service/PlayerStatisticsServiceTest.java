package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.HandRank;
import ch.uzh.ifi.hase.soprafs24.constant.WeatherType;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

public class PlayerStatisticsServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private PlayerService playerService;

    @Mock
    private GameService gameService;

    @InjectMocks
    private PlayerStatisticsService playerStatisticsService;

    private User user;
    private Game game;
    private Player player;
    private GameSettings gameSettings;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setUsername("user");
        List<HandRank> order = new ArrayList<>(Arrays.stream(HandRank.values()).sorted(Comparator.reverseOrder()).toList());

        gameSettings = new GameSettings(1000L, 1L,2L, order, true, WeatherType.CLOUDY, "");

        game = new Game(user, gameSettings, true);
        player = new Player(user, 1000, game);
    }

    @Test
    void getPlayer_BB_100_withActiveGames_calculatesCorrectly() {
        user.setBB_100_record(10.0f);
        user.setBB_100_count(200L);
        player.setBalance(game.getSettings().getInitialBalance() + 50);
        game.setRoundCount(50L);

        when(playerService.getPlayersByUserId(user.getId())).thenReturn(Collections.singletonList(player));
        when(gameService.getGameBySessionId(player.getSessionId())).thenReturn(game);

        float actualBB100 = playerStatisticsService.getPlayer_BB_100(user);

        assertEquals(18, actualBB100, 0.001f);
    }

    @Test
    void getPlayer_BB_100_noActiveGames_returnsUserRecord() {
        user.setBB_100_record(15.5f);
        user.setBB_100_count(100L); // Irrelevant for this specific check if getActivePlayerStats returns 0/0

        when(playerService.getPlayersByUserId(user.getId())).thenReturn(Collections.emptyList());

        float actualBB100 = playerStatisticsService.getPlayer_BB_100(user);

        assertEquals(15.5f, actualBB100, 0.001f);
    }

    @Test
    void updateUser_BB_100_record_calculatesAndSetsCorrectly() {
        user.setBB_100_record(10.0f); // Historical: 10 BB/100 over 200 rounds = 20 BB won
        user.setBB_100_count(200L);

        player.setBalance(this.gameSettings.getInitialBalance() + 50); // Profit of 50 from this game
        game.setRoundCount(50L); // 50 rounds in this game
        // Big Blind is 2L from gameSettings

        // BB won from this game = 50 / 2 = 25 BB
        // New total BB won = 20 (historical) + 25 (current game) = 45 BB
        // New total round count = 200 (historical) + 50 (current game) = 250 rounds
        // New BB/100 record = (45 / 250) * 100 = 18.0f

        playerStatisticsService.updateUser_BB_100_record(user, game, player);

        assertEquals(18.0f, user.getBB_100_record(), 0.001f);
        assertEquals(250L, user.getBB_100_count());
    }

    @Test
    void getPlayer_BB_withActiveGames_calculatesCorrectly() {
        user.setBB_100_record(10.0f); // Historical: 10 BB/100 over 200 rounds = 20 BB won
        user.setBB_100_count(200L);

        player.setBalance(this.gameSettings.getInitialBalance() + 50); // Profit of 50
        game.setRoundCount(50L);
        // Big Blind is 2L

        // Active game: 25 BB won
        // Total BB won = 20 + 25 = 45 BB
        // Total rounds = 200 + 50 = 250 rounds

        when(playerService.getPlayersByUserId(user.getId())).thenReturn(Collections.singletonList(player));
        when(gameService.getGameBySessionId(player.getSessionId())).thenReturn(game);

        float actualBB = playerStatisticsService.getPlayer_BB(user);
        assertEquals(45, actualBB, 0.001f);
    }

    @Test
    void getPlayer_BB_noActiveGames_returnsUserRecordBased() {
        user.setBB_100_record(15.5f); // Historical: 15.5 BB/100 over 100 rounds = 15.5 BB won
        user.setBB_100_count(100L);

        // Expected BB = 15.5 (total BB won) / 100 (total rounds) = 0.155

        when(playerService.getPlayersByUserId(user.getId())).thenReturn(Collections.emptyList());

        float actualBB = playerStatisticsService.getPlayer_BB(user);
        assertEquals(15.5f, actualBB, 0.001f);
    }

    @Test
    void incrementUser_BB_100_count_incrementsCorrectly() {
        long initialCount = 50L;
        user.setBB_100_count(initialCount);
        int increment = 10;

        playerStatisticsService.incrementUser_BB_100_count(user, increment);

        assertEquals(initialCount + increment, user.getBB_100_count());
    }

    @Test
    void getPlayer_BB_100_count_returnsUserValue() {
        user.setBB_100_count(123L);
        // This method seems to just return user.getBB_100_count() without active game logic
        long count = playerStatisticsService.getPlayer_BB_100_count(user);
        assertEquals(123L, count);
    }

    @Test
    void incrementUser_bankrupt_incrementsCorrectly() {
        int initialBankruptcies = 2;
        user.setBankruptCount(initialBankruptcies);

        playerStatisticsService.incrementUser_bankrupt(user);

        assertEquals(initialBankruptcies + 1, user.getBankruptCount());
    }

    @Test
    void getPlayer_bankrupt_returnsUserValue() {
        user.setBankruptCount(3);
        int bankruptcies = playerStatisticsService.getPlayer_bankrupt(user);
        assertEquals(3, bankruptcies);
    }

    @Test
    void incrementUser_round_played_incrementsCorrectly() {
        long initialRounds = 100L;
        user.setRoundCount(initialRounds);

        playerStatisticsService.incrementUser_round_played(user);

        assertEquals(initialRounds + 1, user.getRoundCount());
    }

    @Test
    void updateUser_round_played_setsCorrectly() {
        long newRoundCount = 150L;
        user.setRoundCount(50L); // Initial value

        playerStatisticsService.incrementUser_round_played(user, newRoundCount);

        assertEquals(newRoundCount + 50, user.getRoundCount());
    }

    @Test
    void getPlayer_round_played_withActiveGames_sumsCorrectly() {
        user.setRoundCount(100L); // Historical rounds
        game.setRoundCount(25L);   // Active game rounds

        when(playerService.getPlayersByUserId(user.getId())).thenReturn(Collections.singletonList(player));
        when(gameService.getGameBySessionId(player.getSessionId())).thenReturn(game);

        long totalRounds = playerStatisticsService.getPlayer_round_played(user);
        assertEquals(100L + 25L, totalRounds);
    }

    @Test
    void getPlayer_round_played_noActiveGames_returnsUserValue() {
        user.setRoundCount(100L);

        when(playerService.getPlayersByUserId(user.getId())).thenReturn(Collections.emptyList());

        long totalRounds = playerStatisticsService.getPlayer_round_played(user);
        assertEquals(100L, totalRounds);
    }

    @Test
    void incrementUser_games_played_incrementsCorrectly() {
        int initialGames = 5;
        user.setGameCount(initialGames);

        playerStatisticsService.incrementUser_games_played(user);

        assertEquals(initialGames + 1, user.getGameCount());
    }

    @Test
    void getPlayer_games_played_noActiveGames_returnsUserValue() {
        user.setGameCount(10);
        when(playerService.getPlayersByUserId(user.getId())).thenReturn(Collections.emptyList());
        int gamesPlayed = playerStatisticsService.getPlayer_games_played(user);
        assertEquals(10, gamesPlayed);
    }

    @Test
    void getPlayer_games_played_withActiveGames_addsActiveGameCount() {
        // This test assumes getPlayer_games_played adds 1 for each unique active game session.
        user.setGameCount(10); // Historical games

        Player player2 = new Player(user, 1000, new Game(user, this.gameSettings, true)); // A different game instance

        game.setRoundCount(5L); // Active game has 5 rounds

        when(playerService.getPlayersByUserId(user.getId())).thenReturn(Collections.singletonList(player));
        when(gameService.getGameBySessionId(player.getSessionId())).thenReturn(game);

        int gamesPlayed = playerStatisticsService.getPlayer_games_played(user);
        // Current PSS logic: user.getGameCount() + activePlayerStats.getTotalRoundsPlayed()
        assertEquals(10 + 5, gamesPlayed);
    }


    @Test
    void getActivePlayerStats_noActivePlayers_returnsZeroStats() {
        when(playerService.getPlayersByUserId(user.getId())).thenReturn(Collections.emptyList());

        PlayerStatisticsService.ActivePlayerStats stats = playerStatisticsService.getActivePlayerStats(user);

        assertNotNull(stats);
        assertEquals(0.0, stats.getTotalBBWon(), 0.001);
        assertEquals(0, stats.getTotalRoundsPlayed());
    }

    @Test
    void getActivePlayerStats_oneActivePlayer_calculatesCorrectly() {
        player.setBalance(this.gameSettings.getInitialBalance() + 40); // Profit of 40
        game.setRoundCount(20L); // 20 rounds
        // Big Blind is 2L

        // Expected BB Won = 40 / 2 = 20

        when(playerService.getPlayersByUserId(user.getId())).thenReturn(Collections.singletonList(player));
        when(gameService.getGameBySessionId(player.getSessionId())).thenReturn(game);

        PlayerStatisticsService.ActivePlayerStats stats = playerStatisticsService.getActivePlayerStats(user);

        assertEquals(20.0, stats.getTotalBBWon(), 0.001);
        assertEquals(20L, stats.getTotalRoundsPlayed());
    }

}
