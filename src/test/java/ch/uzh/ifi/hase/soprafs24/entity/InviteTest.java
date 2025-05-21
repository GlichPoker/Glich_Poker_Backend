package ch.uzh.ifi.hase.soprafs24.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import ch.uzh.ifi.hase.soprafs24.constant.HandRank;
import ch.uzh.ifi.hase.soprafs24.constant.WeatherType;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class InviteTest {

    private Invite invite;
    private Game game;
    private User user;
    private GameSettings gameSettings;
    private Player player;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setUsername("user");
        List<HandRank> order = new ArrayList<>(Arrays.stream(HandRank.values()).sorted(Comparator.reverseOrder()).toList());

        gameSettings = new GameSettings(1000L, 1L,2L, order, true, WeatherType.CLOUDY, "");

        game = new Game(user, gameSettings, true);
        player = new Player(user, 1000, game);

        invite = new Invite();

        game = new Game();

        user = new User();
        user.setId(2L);
    }

    @Test
    void defaultConstructor_createsInstance() {
        assertNotNull(invite);
        assertNull(invite.getId());
        assertNull(invite.getGame());
        assertNull(invite.getUser());
    }

    @Test
    void parameterizedConstructor_setsGameUserAndIdCorrectly() {
        Invite newInvite = new Invite(game, user);

        assertNotNull(newInvite.getGame());
        assertEquals(game, newInvite.getGame());

        assertNotNull(newInvite.getUser());
        assertEquals(user, newInvite.getUser());

        assertNotNull(newInvite.getId());
        // According to Invite constructor: this.id = new InviteId(game.getSessionId(), user.getId());
        assertEquals(game.getSessionId(), newInvite.getId().getGameId());
        assertEquals(user.getId(), newInvite.getId().getUserId());
    }

    @Test
    void setId_setsIdCorrectly() {
        InviteId newId = new InviteId(3L, 4L);
        invite.setId(newId);
        assertEquals(newId, invite.getId());
    }

    @Test
    void getId_retrievesIdCorrectly() {
        InviteId newId = new InviteId(3L, 4L);
        invite.setId(newId);
        assertEquals(newId, invite.getId());
    }

    @Test
    void setGame_setsGameCorrectly() {
        invite.setGame(game);
        assertEquals(game, invite.getGame());
    }

    @Test
    void getGame_retrievesGameCorrectly() {
        invite.setGame(game);
        assertEquals(game, invite.getGame());
    }

    @Test
    void setUser_setsUserCorrectly() {
        invite.setUser(user);
        assertEquals(user, invite.getUser());
    }

    @Test
    void getUser_retrievesUserCorrectly() {
        invite.setUser(user);
        assertEquals(user, invite.getUser());
    }

    @Test
    void equals_sameObject_returnsTrue() {
        Invite invite1 = new Invite(game, user);
        assertTrue(invite1.equals(invite1));
    }

    @Test
    void equals_nullObject_returnsFalse() {
        Invite invite1 = new Invite(game, user);
        assertFalse(invite1.equals(null));
    }

    @Test
    void equals_differentClass_returnsFalse() {
        Invite invite1 = new Invite(game, user);
        assertFalse(invite1.equals(new Object()));
    }

    @Test
    void equals_differentId_returnsFalse() {
        Invite invite1 = new Invite(game, user); // id based on (100L, 2L)

        User anotherUser = new User();
        anotherUser.setId(3L); // Different user ID
        Invite invite2 = new Invite(game, anotherUser); // id based on (100L, 3L)

        assertFalse(invite1.equals(invite2));
    }

    @Test
    void equals_oneIdNull_otherNotNull_returnsFalse() {
        Invite inviteWithId = new Invite(game, user); // Has an ID
        Invite inviteWithNullId = new Invite(); // ID is null

        assertFalse(inviteWithId.equals(inviteWithNullId));
        assertFalse(inviteWithNullId.equals(inviteWithId));
    }

    @Test
    void equals_bothIdNull_returnsTrue() {
        Invite invite1WithNullId = new Invite(); // ID is null
        Invite invite2WithNullId = new Invite(); // ID is null

        assertTrue(invite1WithNullId.equals(invite2WithNullId));
    }

    @Test
    void hashCode_consistentWithEquals_sameId() {
        Invite invite1 = new Invite(game, user);
        Invite invite2 = new Invite(game, user);

        // Ensure IDs are identical for the test
        InviteId id = new InviteId(game.getSessionId(), user.getId());
        invite1.setId(id);
        invite2.setId(id);

        assertEquals(invite1.hashCode(), invite2.hashCode());
    }

    @Test
    void hashCode_differentForDifferentIds() {
        Invite invite1 = new Invite(game, user);

        User anotherUser = new User();
        anotherUser.setId(3L);
        Invite invite2 = new Invite(game, anotherUser);

        assertNotEquals(invite1.hashCode(), invite2.hashCode());
    }

    @Test
    void hashCode_bothIdNull_sameHashCode() {
        Invite invite1WithNullId = new Invite();
        Invite invite2WithNullId = new Invite();
        assertEquals(invite1WithNullId.hashCode(), invite2WithNullId.hashCode());
    }
}