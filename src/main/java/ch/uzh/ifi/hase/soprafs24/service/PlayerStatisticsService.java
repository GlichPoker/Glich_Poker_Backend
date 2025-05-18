package ch.uzh.ifi.hase.soprafs24.service;

import java.util.List;

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

    public PlayerStatisticsService(UserService userService, GameRepository gameRepository) {
        this.userService = userService;
        this.gameRepository = gameRepository;
    }

    public void updateUser_BB_100_record(User user, Game game, Player player){
        float bb100_record = user.getBB_100_record();
        long bb_100_count = user.getBB_100_count();
        double bb_won = bb100_record * (bb_100_count / 100.0);

        double profits = player.getBalance() - game.getSettings().getInitialBalance();
        double bb_won_new = profits / game.getSettings().getBigBlind();
        bb_won = bb_won + bb_won_new;
        bb_100_count = bb_100_count + game.getRoundCount();

        bb100_record = (float) (bb_won * (100.0 / bb_100_count));
        user.setBB_100_record(bb100_record);
        user.setBB_100_count(bb_100_count);
    }

    public float getPlayer_BB_100(User user){
        float bb100_record = user.getBB_100_record();
        long bb_100_count_record = user.getBB_100_count();
        float bbwon_record = bb100_record * (bb_100_count_record / 100.0f);

        List<Game> activeGames = gameRepository.findActiveGamesByUser(user);
        double totalBBWon = 0.0;
        long totalRoundsPlayed = 0;

        for (Game activeGame : activeGames) {
            // Find the specific player object for the user in this active game
            Player currentPlayerInGame = null;
            for (Player p : activeGame.getPlayers()) {
                if (p.getUser() != null && p.getUser().getId().equals(user.getId())) {
                    currentPlayerInGame = p;
                    break;
                }
            }

            if (currentPlayerInGame != null && activeGame.getSettings() != null) {
                double initialBalance = activeGame.getSettings().getInitialBalance();
                double bigBlind = activeGame.getSettings().getBigBlind();
                double currentBalance = currentPlayerInGame.getBalance();
                long roundsInThisGame = activeGame.getRoundCount();

                if (bigBlind > 0) { // Avoid division by zero
                    double profitsInActiveGame = currentBalance - initialBalance;
                    double bbWonInActiveGame = profitsInActiveGame / bigBlind;

                    totalBBWon += bbWonInActiveGame;
                    totalRoundsPlayed += roundsInThisGame;
                }
            }
        }

        if (totalRoundsPlayed == 0) {
            return 0.0f; // Or handle as per your application's logic (e.g., NaN, throw exception)
        }

        totalBBWon += bbwon_record;
        totalRoundsPlayed += bb_100_count_record;

        return (float) ((totalBBWon / totalRoundsPlayed) * 100.0);
    }

    public void incrementUser_BB_100_count(User user, int additional_round){
        user.setBB_100_count(user.getBB_100_count() + additional_round);
        userService.saveUser(user);
    }

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

    public void updateUser_round_played(User user, long additional_round){
        user.setRoundCount(user.getRoundCount() + additional_round);
        userService.saveUser(user);
    }

    public long getPlayer_round_played(User user){
        return user.getRoundCount();
    }

    public void incrementUser_games_played(User user){
        user.setGameCount(user.getGameCount() + 1);
        userService.saveUser(user);
    }

    public int getPlayer_games_played(User user){
        return user.getGameCount();
    }

}
