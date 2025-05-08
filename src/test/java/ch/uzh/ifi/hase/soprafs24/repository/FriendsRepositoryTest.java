package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.constant.FriendRequestState;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Friends;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class FriendsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FriendsRepository friendsRepository;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("pw1");
        user1.setStatus(UserStatus.ONLINE);
        user1.setToken("token1");

        user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("pw2");
        user2.setStatus(UserStatus.ONLINE);
        user2.setToken("token2");

        user3 = new User();
        user3.setUsername("user3");
        user3.setPassword("pw3");
        user3.setStatus(UserStatus.ONLINE);
        user3.setToken("token3");

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        entityManager.flush();
    }

    @Test
    void testFindAllFriendsSuccess() {
        Friends friendship = new Friends();
        friendship.setUser1(user1);
        friendship.setUser2(user2);
        friendship.setRequestStatus(FriendRequestState.ACCEPTED);
        entityManager.persist(friendship);
        entityManager.flush();

        List<User> friendsOfUser1 = friendsRepository.findAllFriends(user1.getId());
        assertEquals(1, friendsOfUser1.size());
        assertEquals(user2.getId(), friendsOfUser1.get(0).getId());
    }

    @Test
    void testFindAllPendingRequestsSuccess() {
        Friends request = new Friends();
        request.setUser1(user1);
        request.setUser2(user3);
        request.setRequestStatus(FriendRequestState.PENDING);
        entityManager.persist(request);
        entityManager.flush();

        List<User> pendingRequests = friendsRepository.findAllPendingRequests(user1.getId());
        assertEquals(1, pendingRequests.size());
        assertEquals(user3.getId(), pendingRequests.get(0).getId());
    }

    @Test
    void testFindByUser1IdAndUser2IdAndStatusSuccess() {
        Friends friendship = new Friends();
        friendship.setUser1(user1);
        friendship.setUser2(user2);
        friendship.setRequestStatus(FriendRequestState.ACCEPTED);
        entityManager.persist(friendship);
        entityManager.flush();

        Friends found = friendsRepository.findByUser1IdAndUser2IdAndStatus(
                user1.getId(), user2.getId(), FriendRequestState.ACCEPTED);

        assertNotNull(found);
        assertEquals(user1.getId(), found.getUser1().getId());
        assertEquals(user2.getId(), found.getUser2().getId());
    }

    @Test
    void testExistsByUser1IdAndUser2IdAndStatusTrue() {
        Friends friendship = new Friends();
        friendship.setUser1(user2);
        friendship.setUser2(user3);
        friendship.setRequestStatus(FriendRequestState.ACCEPTED);
        entityManager.persist(friendship);
        entityManager.flush();

        boolean exists = friendsRepository.existsByUser1IdAndUser2IdAndStatus(
                user2.getId(), user3.getId(), FriendRequestState.ACCEPTED);
        assertTrue(exists);
    }

    @Test
    void testExistsByUser1IdAndUser2IdAndStatusFalse() {
        boolean exists = friendsRepository.existsByUser1IdAndUser2IdAndStatus(
                user1.getId(), user3.getId(), FriendRequestState.ACCEPTED);
        assertFalse(exists);
    }
}
