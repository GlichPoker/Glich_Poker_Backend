package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.GameSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class GameSettingsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GameSettingsRepository gameSettingsRepository;

    private GameSettings settings;

    @BeforeEach
    public void setUp() {
        settings = new GameSettings();
        entityManager.persist(settings);
        entityManager.flush();
    }

    @Test
    public void testFindByIdSuccess() {
        Optional<GameSettings> found = gameSettingsRepository.findById(settings.getId());

        assertTrue(found.isPresent());
        assertEquals(settings.getId(), found.get().getId());
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<GameSettings> notFound = gameSettingsRepository.findById(999L);

        assertFalse(notFound.isPresent());
    }
}
