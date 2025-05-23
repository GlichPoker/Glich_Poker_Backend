package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.HandRank;
import ch.uzh.ifi.hase.soprafs24.constant.WeatherType;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.GameSettings;
import ch.uzh.ifi.hase.soprafs24.entity.Invite;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.GameInvitesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class InviteGameServiceTest {

    @Mock
    private GameInvitesRepository gameInvitesRepository;

    @Mock
    private PlayerService playerService;

    @Mock
    private GameService gameService;

    @InjectMocks
    private InviteGameService inviteGameService;

    private User testUser;
    private User hostUser;
    private Game testGame;
    private GameSettings gameSettings;
    private Invite testInvite;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        hostUser = new User();
        hostUser.setId(1L);
        hostUser.setUsername("hostUser");

        testUser = new User();
        testUser.setId(2L);
        testUser.setUsername("testUser");

        List<HandRank> order = new ArrayList<>(Arrays.stream(HandRank.values()).sorted(Comparator.reverseOrder()).toList());

        gameSettings = new GameSettings(1000L, 1L,2L, order, true, WeatherType.CLOUDY, "");

        testGame = new Game(hostUser, gameSettings, true);
        //testGame.setSessionId(200L); // Explicitly set session ID for game

        testInvite = new Invite(testGame, testUser);
    }

    @Test
    void addAllowedUser_userNotAllowed_savesInvite() {
        when(gameInvitesRepository.findById_GameIdAndId_UserId(testGame.getSessionId(), testUser.getId()))
                .thenReturn(Optional.empty());

        inviteGameService.addAllowedUser(testGame, testUser);

        verify(gameInvitesRepository, times(1)).save(any(Invite.class));
        verify(gameInvitesRepository, times(1)).flush();
    }

    @Test
    void addAllowedUser_userAlreadyAllowed_doesNotSaveInvite() {
        when(gameInvitesRepository.findById_GameIdAndId_UserId(testGame.getSessionId(), testUser.getId()))
                .thenReturn(Optional.of(testInvite));

        inviteGameService.addAllowedUser(testGame, testUser);

        verify(gameInvitesRepository, never()).save(any(Invite.class));
        verify(gameInvitesRepository, never()).flush();
    }

    @Test
    void acceptInvite_userNotAllowed_doesNothing() {
        when(gameInvitesRepository.findById_GameIdAndId_UserId(testGame.getSessionId(), testUser.getId()))
                .thenReturn(Optional.empty());

        inviteGameService.acceptInvite(testGame, testUser);

        verify(gameService, never()).createAndAddPlayerToGame(any(Game.class), any(User.class), anyLong());
        verify(gameInvitesRepository, never()).delete(any(Invite.class));
        verify(gameInvitesRepository, never()).flush();
    }

    @Test
    void rejectInvite_userAllowed_removesInvite() {
        when(gameInvitesRepository.findById_GameIdAndId_UserId(testGame.getSessionId(), testUser.getId()))
                .thenReturn(Optional.of(testInvite));

        inviteGameService.rejectInvite(testGame, testUser);

        verify(gameInvitesRepository, times(1)).delete(testInvite);
        verify(gameInvitesRepository, times(1)).flush();
    }

    @Test
    void rejectInvite_userNotAllowed_doesNothing() {
        when(gameInvitesRepository.findById_GameIdAndId_UserId(testGame.getSessionId(), testUser.getId()))
                .thenReturn(Optional.empty());

        inviteGameService.rejectInvite(testGame, testUser);

        verify(gameInvitesRepository, never()).delete(any(Invite.class));
        verify(gameInvitesRepository, never()).flush();
    }

    @Test
    void getOpenInvitations_userHasInvitations_returnsListOfGames() {
        List<Invite> invites = Collections.singletonList(testInvite);
        when(gameInvitesRepository.findById_UserId(testUser.getId())).thenReturn(invites);

        List<Game> openGames = inviteGameService.getOpenInvitations(testUser.getId());

        assertNotNull(openGames);
        assertEquals(1, openGames.size());
        assertEquals(testGame.getSessionId(), openGames.get(0).getSessionId());
    }

    @Test
    void getOpenInvitations_userHasNoInvitations_returnsEmptyList() {
        when(gameInvitesRepository.findById_UserId(testUser.getId())).thenReturn(Collections.emptyList());

        List<Game> openGames = inviteGameService.getOpenInvitations(testUser.getId());

        assertNotNull(openGames);
        assertTrue(openGames.isEmpty());
    }

    @Test
    void isUserAllowed_userIsAllowed_returnsTrue() {
        when(gameInvitesRepository.findById_GameIdAndId_UserId(testGame.getSessionId(), testUser.getId()))
                .thenReturn(Optional.of(testInvite));

        boolean isAllowed = inviteGameService.isUserAllowed(testGame.getSessionId(), testUser.getId());

        assertTrue(isAllowed);
    }

    @Test
    void isUserAllowed_userIsNotAllowed_returnsFalse() {
        when(gameInvitesRepository.findById_GameIdAndId_UserId(testGame.getSessionId(), testUser.getId()))
                .thenReturn(Optional.empty());

        boolean isAllowed = inviteGameService.isUserAllowed(testGame.getSessionId(), testUser.getId());

        assertFalse(isAllowed);
    }

    @Test
    void getAllowedUsers_gameHasAllowedUsers_returnsListOfUsers() {
        List<Invite> invites = Collections.singletonList(testInvite);
        when(gameInvitesRepository.findById_GameId(testGame.getSessionId())).thenReturn(invites);

        List<User> allowedUsers = inviteGameService.getAllowedUsers(testGame.getSessionId());

        assertNotNull(allowedUsers);
        assertEquals(1, allowedUsers.size());
        assertEquals(testUser.getId(), allowedUsers.get(0).getId());
    }

    @Test
    void getAllowedUsers_gameHasNoAllowedUsers_returnsEmptyList() {
        when(gameInvitesRepository.findById_GameId(testGame.getSessionId())).thenReturn(Collections.emptyList());

        List<User> allowedUsers = inviteGameService.getAllowedUsers(testGame.getSessionId());

        assertNotNull(allowedUsers);
        assertTrue(allowedUsers.isEmpty());
    }

    @Test
    void removeAllowedUser_userIsAllowed_deletesInvite() {
        when(gameInvitesRepository.findById_GameIdAndId_UserId(testGame.getSessionId(), testUser.getId()))
                .thenReturn(Optional.of(testInvite));

        inviteGameService.removeAllowedUser(testGame.getSessionId(), testUser.getId());

        verify(gameInvitesRepository, times(1)).delete(testInvite);
        verify(gameInvitesRepository, times(1)).flush();
    }

    @Test
    void removeAllowedUser_userIsNotAllowed_doesNotDelete() {
        when(gameInvitesRepository.findById_GameIdAndId_UserId(testGame.getSessionId(), testUser.getId()))
                .thenReturn(Optional.empty());

        inviteGameService.removeAllowedUser(testGame.getSessionId(), testUser.getId());

        verify(gameInvitesRepository, never()).delete(any(Invite.class));
        verify(gameInvitesRepository, never()).flush();
    }
}