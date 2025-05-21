package ch.uzh.ifi.hase.soprafs24.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

@Service
@Transactional
public class PlayerStatisticsService {

    @Autowired
    private UserService userService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private GameService gameService;

    public static class ActivePlayerStats {
        private final double totalBBWon;
        private final long totalRoundsPlayed;

        public ActivePlayerStats(double totalBBWon, long totalRoundsPlayed) {
            this.totalBBWon = totalBBWon;
            this.totalRoundsPlayed = totalRoundsPlayed;
        }

        public double getTotalBBWon() {
            return totalBBWon;
        }

        public long getTotalRoundsPlayed() {
            return totalRoundsPlayed;
        }
    }

    public PlayerStatisticsService(UserService userService, GameRepository gameRepository, PlayerService playerService, GameService gameService) {
        this.gameService = gameService;
        this.userService = userService;
        this.gameRepository = gameRepository;
        this.playerService = playerService;
    }

    public void updateUser_BB_100_record(User user, Game game, Player player){
        float bb100_record = user.getBB_100_record();
        long bb_100_count = user.getBB_100_count();
        double bb_won = bb100_record * (bb_100_count / 100.0);

        double profits = player.getBalance() - game.getSettings().getInitialBalance();
        double bb_won_new = profits / game.getSettings().getBigBlind();
        bb_won = bb_won + bb_won_new;
        bb_100_count = bb_100_count + game.getRoundCount();

        // Handle division by zero for bb_100_count if it's 0 after updates
        if (bb_100_count == 0) {
            bb100_record = 0.0f;
        } else {
            bb100_record = (float) (bb_won * (100.0 / bb_100_count));
        }
        user.setBB_100_record(bb100_record);
        user.setBB_100_count(bb_100_count);
    }

    public float getPlayer_BB_100(User user){
        float bb100_record = user.getBB_100_record();
        long bb_100_count_record = user.getBB_100_count();
        float bbwon_record = bb100_record * (bb_100_count_record / 100.0f);
        double totalBBWon = 0.0;
        long totalRoundsPlayed = 0;

        totalBBWon += bbwon_record;
        totalRoundsPlayed += bb_100_count_record;

        ActivePlayerStats activePlayerStats = getActivePlayerStats(user);

        totalBBWon += activePlayerStats.getTotalBBWon();
        totalRoundsPlayed += activePlayerStats.getTotalRoundsPlayed();

        if (totalRoundsPlayed == 0) {
            return 0.0f;
        }

        return (float) ((totalBBWon / totalRoundsPlayed) * 100.0);
    }

    public float getPlayer_BB(User user){
        float bb100_record = user.getBB_100_record();
        long bb_100_count_record = user.getBB_100_count();
        float bbwon_record = bb100_record * (bb_100_count_record / 100.0f);
        double totalBBWon = 0.0;
        long totalRoundsPlayed = 0;

        totalBBWon += bbwon_record;
        totalRoundsPlayed += bb_100_count_record;

        ActivePlayerStats activePlayerStats = getActivePlayerStats(user);

        totalBBWon += activePlayerStats.getTotalBBWon();
        totalRoundsPlayed += activePlayerStats.getTotalRoundsPlayed();

        return (float) (totalBBWon);
    }

    public ActivePlayerStats getActivePlayerStats(User user){
        double totalBBWon = 0.0;
        long totalRoundsPlayed = 0;

        List<Player> players = playerService.getPlayersByUserId(user.getId());

        for (Player player : players) {
            Long sessionId = player.getSessionId();
            Game game = gameService.getGameBySessionId(sessionId);

            if (game == null) {
                System.err.println("Game not found for sessionId: " + sessionId);
                continue;
            }

            double initialBalance = game.getSettings().getInitialBalance();
            double bigBlind = game.getSettings().getBigBlind();
            double currentBalance = player.getBalance();

            if(bigBlind == 0){
                System.err.println("Big blind is 0 for game with sessionId: " + sessionId);
                continue;
            }

            double profitsInActiveGame = currentBalance - initialBalance;
            totalBBWon += profitsInActiveGame / bigBlind;
            totalRoundsPlayed += game.getRoundCount();
        }

        return new ActivePlayerStats(totalBBWon, totalRoundsPlayed);
    }

    public Map<User, ActivePlayerStats> getAllPlayersBB100(){
        List<User> users = userService.getAllUsers();
        Map<User, ActivePlayerStats> userStatmap = new HashMap<>();
        
        for (User user : users) {
            ActivePlayerStats activePlayerStats = getActivePlayerStats(user);
            userStatmap.put(user, activePlayerStats);
        }
        return userStatmap;
    }

    // With "count" I mean the number of rounds played that have been considered for the BB_100 record
    public void incrementUser_BB_100_count(User user, int additional_round){
        user.setBB_100_count(user.getBB_100_count() + additional_round);
        userService.saveUser(user);
    }

    // With "count" I mean the number of rounds played that have been considered for the BB_100 record
    public long getPlayer_BB_100_count(User user){
        return user.getBB_100_count();
    }

    public void incrementUser_bankrupt(User user){
        user.setBankruptCount(user.getBankruptCount() + 1);
        userService.saveUser(user);
    }

    public int getPlayer_bankrupt(User user){
        return user.getBankruptCount();
    }

    public void incrementUser_round_played(User user){
        user.setRoundCount(user.getRoundCount() + 1);
        userService.saveUser(user);
    }

    public void incrementUser_round_played(User user, long additional_round){
        user.setRoundCount(user.getRoundCount() + additional_round);
        userService.saveUser(user);
    }

    public long getPlayer_round_played(User user){
        ActivePlayerStats activePlayerStats = getActivePlayerStats(user);
        return user.getRoundCount() + activePlayerStats.getTotalRoundsPlayed();
    }

    public void incrementUser_games_played(User user){
        user.setGameCount(user.getGameCount() + 1);
        userService.saveUser(user);
    }

    public int getPlayer_games_played(User user){
        ActivePlayerStats activePlayerStats = getActivePlayerStats(user);
        return user.getGameCount() + (int) activePlayerStats.getTotalRoundsPlayed();
    }

}
