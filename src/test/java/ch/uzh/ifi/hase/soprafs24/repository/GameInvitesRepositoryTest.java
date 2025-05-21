package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.constant.HandRank;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.constant.WeatherType;
import ch.uzh.ifi.hase.soprafs24.entity.*;
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
class GameInvitesRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GameInvitesRepository gameInvitesRepository;

    private User hostUser;
    private User invitedUser;
    private User anotherUser;
    private GameSettings gameSettings;
    private Game game1;
    private Game game2;
    private Invite invite1;
    private Invite invite2;

    @BeforeEach
    void setUp() {
        // Create and persist host user
        hostUser = new User();
        hostUser.setUsername("hostUser");
        hostUser.setPassword("hostPassword");
        hostUser.setStatus(UserStatus.ONLINE);
        hostUser.setToken("hostToken");
        entityManager.persist(hostUser);

        // Create and persist invited user
        invitedUser = new User();
        invitedUser.setUsername("invitedUser");
        invitedUser.setPassword("invitedPassword");
        invitedUser.setStatus(UserStatus.ONLINE);
        invitedUser.setToken("invitedToken");
        entityManager.persist(invitedUser);

        // Create and persist another user
        anotherUser = new User();
        anotherUser.setUsername("anotherUser");
        anotherUser.setPassword("anotherPassword");
        anotherUser.setStatus(UserStatus.ONLINE);
        anotherUser.setToken("anotherToken");
        entityManager.persist(anotherUser);

        // Create game settings
        List<HandRank> order = new ArrayList<>(Arrays.stream(HandRank.values()).sorted(Comparator.reverseOrder()).toList());
        gameSettings = new GameSettings(1000L, 5L, 10L, order, true, WeatherType.SUNNY, "");
        entityManager.persist(gameSettings);

        // Create and persist game1 hosted by hostUser
        game1 = new Game(hostUser, gameSettings, true);
        entityManager.persist(game1);

        // Create and persist game2 hosted by hostUser
        game2 = new Game(hostUser, gameSettings, false); // A different game
        entityManager.persist(game2);

        // Create and persist invite1: invitedUser to game1
        invite1 = new Invite(game1, invitedUser);
        entityManager.persist(invite1);

        // Create and persist invite2: anotherUser to game1
        invite2 = new Invite(game1, anotherUser);
        entityManager.persist(invite2);

        // Create and persist an invite for invitedUser to game2
        Invite invite3 = new Invite(game2, invitedUser);
        entityManager.persist(invite3);

        entityManager.flush();
    }

    @Test
    void findById_GameIdAndId_UserId_success() {
        // when
        Optional<Invite> foundInvite = gameInvitesRepository.findById_GameIdAndId_UserId(game1.getSessionId(), invitedUser.getId());

        // then
        assertTrue(foundInvite.isPresent());
        assertEquals(game1.getSessionId(), foundInvite.get().getGame().getSessionId());
        assertEquals(invitedUser.getId(), foundInvite.get().getUser().getId());
    }

    @Test
    void findById_GameIdAndId_UserId_notFound_wrongGameId() {
        // when
        Optional<Invite> foundInvite = gameInvitesRepository.findById_GameIdAndId_UserId(999L, invitedUser.getId());

        // then
        assertFalse(foundInvite.isPresent());
    }

    @Test
    void findById_GameIdAndId_UserId_notFound_wrongUserId() {
        // when
        Optional<Invite> foundInvite = gameInvitesRepository.findById_GameIdAndId_UserId(game1.getSessionId(), 999L);

        // then
        assertFalse(foundInvite.isPresent());
    }

    @Test
    void findById_GameId_success() {
        // when
        List<Invite> foundInvites = gameInvitesRepository.findById_GameId(game1.getSessionId());

        // then
        assertNotNull(foundInvites);
        assertEquals(2, foundInvites.size()); // invite1 and invite2 are for game1
        assertTrue(foundInvites.stream().anyMatch(inv -> inv.getUser().getId().equals(invitedUser.getId())));
        assertTrue(foundInvites.stream().anyMatch(inv -> inv.getUser().getId().equals(anotherUser.getId())));
    }

    @Test
    void findById_GameId_empty() {
        // Create a new game with no invites
        Game game3 = new Game(hostUser, gameSettings, true);
        entityManager.persist(game3);
        entityManager.flush();

        // when
        List<Invite> foundInvites = gameInvitesRepository.findById_GameId(game3.getSessionId());

        // then
        assertNotNull(foundInvites);
        assertTrue(foundInvites.isEmpty());
    }

    @Test
    void findById_UserId_success() {
        // when
        List<Invite> foundInvites = gameInvitesRepository.findById_UserId(invitedUser.getId());

        // then
        assertNotNull(foundInvites);
        assertEquals(2, foundInvites.size()); // invitedUser has invites for game1 and game2
        assertTrue(foundInvites.stream().anyMatch(inv -> inv.getGame().getSessionId() == (game1.getSessionId())));
        assertTrue(foundInvites.stream().anyMatch(inv -> inv.getGame().getSessionId() == game2.getSessionId()));
    }

}