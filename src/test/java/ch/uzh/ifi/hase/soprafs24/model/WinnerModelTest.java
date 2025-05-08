package ch.uzh.ifi.hase.soprafs24.model;


import ch.uzh.ifi.hase.soprafs24.constant.WeatherType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WinnerModelTest {

    private Round round;

    @BeforeEach
    void setup(){
        List<Player> players = new ArrayList<>() {{
            add(new Player(1, "a", 100));
            add(new Player(2, "b", 100));
            add(new Player(3, "c", 100));
        }};
        ch.uzh.ifi.hase.soprafs24.model.GameSettings gameSettings = new GameSettings(1000, 10, 20, null, true, WeatherType.RAINY, "");
        round = new Round(players, 0, gameSettings);
    }
    @Test
    void testGetWinnings(){
        WinnerModel winnerModel = new WinnerModel(round, 1, new HashMap<Long, Double>(){{put(1L, 40.0);}});
        assertEquals(1, winnerModel.getWinnings().size());
        assertTrue(winnerModel.getWinnings().containsKey(1L));
        assertEquals(40.0, winnerModel.getWinnings().get(1L));

    }
}
