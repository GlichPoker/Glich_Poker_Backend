package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.Model;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.websockets.WS_Handler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ModelPusher {
    public void pushModel(Round round, ch.uzh.ifi.hase.soprafs24.model.Game game, WS_Handler wsHandler, GameService gameService) {
        if (round.isRoundOver()) {
            Map<Long, Double> winnings = round.onRoundCompletion(game.getSettings());
            for (Player p : game.getPlayers()) {
                WinnerModel winnerModel = new WinnerModel(round, p.getUserId(), winnings, game.getSettings().weatherType());
                wsHandler.sendRawWinnerModelToPlayer(
                        String.valueOf(game.getSessionId()),
                        p.getUserId(),
                        winnerModel);
            }
            gameService.completeRound(game);
        } else {
            wsHandler.sendModelToAll(Long.toString(game.getSessionId()), game, Model.ROUNDMODEL);
        }
    }
}