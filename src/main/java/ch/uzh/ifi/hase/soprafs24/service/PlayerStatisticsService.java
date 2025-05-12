package ch.uzh.ifi.hase.soprafs24.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.User;

@Service
@Transactional
public class PlayerStatisticsService {

    @Autowired
    private UserService userService;

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

        //*Some of these variables would be needid if we want to include the currently running games
        //int bb_100_count = user.getBB_100_count();
        //int round_count = user.getRoundCount();
        //int starting_balance;
        //int current_balance;
        //int game_count = ;

        return bb100_record;
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
