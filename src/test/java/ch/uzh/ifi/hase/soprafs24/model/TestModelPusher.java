package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.HandRank;
import ch.uzh.ifi.hase.soprafs24.constant.Model;
import ch.uzh.ifi.hase.soprafs24.constant.WeatherType;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.websockets.WS_Handler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TestModelPusher {
    @InjectMocks
    private ModelPusher modelPusher;
    private Round round;
    private Game game;
    @Mock
    private WS_Handler wsHandler;
    @Mock
    private GameService gameService;

    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);
        List<Player> players = new ArrayList<>() {{
            add(new Player(1, "a", 100));
            add(new Player(2, "b", 100));
            add(new Player(3, "c", 100));
            add(new Player(10, "hallo", 100));
        }};
        List<HandRank> order = new ArrayList<>(Arrays.stream(HandRank.values()).sorted(Comparator.reverseOrder()).toList());

        GameSettings gameSettings = new GameSettings(1000,10,20,order, true, WeatherType.RAINY, "");
        game = new Game(players.get(3), gameSettings);
        round = new Round(players, 0, true, gameSettings);
    }

    @Test
    void testModelPusher(){
        modelPusher.pushModel(round, game, wsHandler, gameService);
        verify(wsHandler, times(1)).sendModelToAll(any(), eq(game), eq(Model.ROUNDMODEL));
    }

    @Test
    void testModelPusherRoundOver(){
        round.setIsRoundOver(true);
        modelPusher.pushModel(round, game, wsHandler, gameService);
        verify(wsHandler, times(1)).sendRawWinnerModelToPlayer(any(), anyLong(), any());

    }
}
