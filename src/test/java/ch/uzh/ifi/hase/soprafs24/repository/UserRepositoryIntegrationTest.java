package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setPassword("securePassword");
        user.setUsername("testUser");
        user.setStatus(UserStatus.ONLINE);
        user.setToken("testToken");
        entityManager.persist(user);
        entityManager.flush();
    }

    @Test
    void testFindByUsernameSuccess() {
        User found = userRepository.findByUsername(user.getUsername());

        assertNotNull(found);
        assertEquals(user.getUsername(), found.getUsername());
        assertEquals(user.getPassword(), found.getPassword());
        assertEquals(user.getStatus(), found.getStatus());
        assertEquals(user.getToken(), found.getToken());
    }

    @Test
    void testFindByUsernameNotFound() {
        User notFound = userRepository.findByUsername("nonExistentUser");

        assertNull(notFound);
    }

    @Test
    void testFindByTokenSuccess() {
        User found = userRepository.findByToken(user.getToken());

        assertNotNull(found);
        assertEquals(user.getToken(), found.getToken());
        assertEquals(user.getUsername(), found.getUsername());
    }

    @Test
    void testFindByTokenNotFound() {
        User notFound = userRepository.findByToken("nonExistentToken");

        assertNull(notFound);
    }

    @Test
    void testFindByIdSuccess() {
        Optional<User> found = userRepository.findById(user.getId());

        assertTrue(found.isPresent());
        assertEquals(user, found.get());
    }

    @Test
    void testFindByIdNotFound() {
        Optional<User> notFound = userRepository.findById(99L);

        assertFalse(notFound.isPresent());
        assertThrows(NoSuchElementException.class, notFound::get);
    }

    @Test
    void testGetAllUsersSuccess() {
        User user2 = new User();
        user2.setUsername("anotherUser");
        user2.setPassword("password");
        user2.setStatus(UserStatus.OFFLINE);
        user2.setToken("token2");
        entityManager.persist(user2);
        entityManager.flush();

        List<User> users = userRepository.getAllUsersExceptSelf(user.getId());

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals(user2.getUsername(), users.get(0).getUsername());
    }

    @Test
    void testGetAllUsersNoUsers() {
        List<User> users = userRepository.getAllUsersExceptSelf(user.getId());

        assertTrue(users.isEmpty());
    }
}
