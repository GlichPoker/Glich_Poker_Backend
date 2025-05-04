package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.constant.HandRank;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.GameSettings;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class PlayerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PlayerRepository playerRepository;

    private User user;
    private Game game;
    private Player player;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setUsername("user1");
        user.setPassword("pw1");
        user.setStatus(UserStatus.ONLINE);
        user.setToken("token1");
        entityManager.persist(user);
        List<HandRank> order = new ArrayList<>(Arrays.stream(HandRank.values()).sorted(Comparator.reverseOrder()).toList());

        GameSettings gameSettings = new GameSettings(1000, 5, 10, order, true);
        entityManager.persist(gameSettings);

        game = new Game(user, gameSettings, true);
        entityManager.persist(game);

        player = new Player(user, 1000, game);
        entityManager.persist(player);
        entityManager.flush();
    }

    @Test
    public void testFindByUserIdSuccess() {
        Optional<Player> found = playerRepository.findByUserId(user.getId());

        assertTrue(found.isPresent());
        assertEquals(player.getUserId(), found.get().getUserId());
        assertEquals(player.getBalance(), found.get().getBalance());
        assertEquals(player.getName(), found.get().getName());
    }

    @Test
    public void testFindByUserIdNotFound() {
        Optional<Player> notFound = playerRepository.findByUserId(999L);

        assertFalse(notFound.isPresent());
    }

    @Test
    public void testFindByGameIdSuccess() {
        List<Player> players = playerRepository.findByGameId(game.getSessionId());

        assertNotNull(players);
        assertEquals(1, players.size());
        assertEquals(player, players.get(0));
    }

    @Test
    public void testFindByGameId_empty() {
        Game newGame = new Game();
        entityManager.persist(newGame);
        entityManager.flush();

        List<Player> players = playerRepository.findByGameId(newGame.getSessionId());

        assertTrue(players.isEmpty());
    }
}
