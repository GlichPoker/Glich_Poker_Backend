package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.WeatherType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameTest {

    @Test
    public void testGameConstructorAndAddPlayer() {
        Game game = new Game(new Player(1,"hallo", 100), new GameSettings(1000,10,20, null, true, WeatherType.RAINY, ""));
        game.addPlayer(new Player(2, "das", 200));
        assertEquals(2, game.getPlayers().size());
    }

    @Test
    public void testRemovePlayer() {
        Game game = new Game(new Player(1,"hallo", 100), new GameSettings(1000,10,20, null, true, WeatherType.RAINY, ""));
        Player player = new Player(2, "das", 200);
        game.addPlayer(player);
        assertEquals(2, game.getPlayers().size());
        game.removePlayer(2);
        assertEquals(1, game.getPlayers().size());
    }

    @Test
    public void testGetPlayer() {
        Player player = new Player(1,"hallo", 100);
        Game game = new Game(player, new GameSettings(1000,10,20, null, true, WeatherType.RAINY, ""));
        assertEquals(player, game.getPlayer(1));
    }

    @Test
    public void testContainsUser() {
        Player player = new Player(1,"hallo", 100);
        Game game = new Game(player, new GameSettings(1000,10,20, null, true, WeatherType.RAINY, ""));
        assertTrue(game.containsUser(1));
    }

    @Test
    public void testJoinSession(){
        Player player = new Player(1,"hallo", 100);
        Game game = new Game(player, new GameSettings(1000,10,20, null, true, WeatherType.RAINY, ""));
        game.joinSession(1);
        assertTrue(game.getPlayer(1).isOnline());
    }

    @Test
    public void testJoinSessionPlayerNotInGame(){
        Player player = new Player(1,"hallo", 100);
        Game game = new Game(player, new GameSettings(1000,10,20, null, true, WeatherType.RAINY, ""));
        game.joinSession(2);
        assertFalse(game.getPlayer(1).isOnline());
    }

    @Test
    public void testAdjustSettings(){
        Player player = new Player(1,"hallo", 100);
        GameSettings gameSettings = new GameSettings(1000,10,20, null, true, WeatherType.RAINY, "");
        Game game = new Game(player, gameSettings);
        GameSettings gameSettings2 = new GameSettings(2000,10,20, null, true, WeatherType.RAINY, "");
        game.adjustSettings(gameSettings2);
        assertEquals(2000, game.getSettings().initialBalance());
    }
}
