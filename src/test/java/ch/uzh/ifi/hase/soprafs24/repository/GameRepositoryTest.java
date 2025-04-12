package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.GameSettings;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class GameRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GameRepository gameRepository;

    private User owner;

    private GameSettings gameSettings;

    @BeforeEach
    public void setUp() {
        owner = new User();
        owner.setUsername("gameOwner");
        owner.setPassword("password");
        owner.setStatus(UserStatus.ONLINE);
        owner.setToken("ownerToken");
        gameSettings = new GameSettings(1000, 5, 10);

        entityManager.persist(owner);
        entityManager.persist(gameSettings);
        entityManager.flush();
    }

    @Test
    public void testFindByIdSuccess() {
        Game game = new Game(owner, gameSettings,true);

        entityManager.persist(game);
        entityManager.flush();

        Optional<Game> found = gameRepository.findById(game.getSessionId());

        assertTrue(found.isPresent());
        assertEquals(owner.getId(), found.get().getOwner().getId());
    }

    @Test
    public void testGetGamesByOwnerIdSuccess() {
        Game game1 = new Game(owner, gameSettings,true);
        Game game2 = new Game(owner, gameSettings,true);

        entityManager.persist(game1);
        entityManager.persist(game2);
        entityManager.flush();

        List<Game> games = gameRepository.getGamesByOwnerId(owner.getId());

        assertEquals(2, games.size());
        assertTrue(games.stream().allMatch(g -> g.getOwner().getId().equals(owner.getId())));
    }

    @Test
    public void testFindAllSuccess() {
        Game game = new Game(owner, gameSettings,true);

        entityManager.persist(game);
        entityManager.flush();

        List<Game> games = gameRepository.findAll();

        assertEquals(1, games.size());
        assertEquals(game.getSessionId(), games.get(0).getSessionId());
    }
}
