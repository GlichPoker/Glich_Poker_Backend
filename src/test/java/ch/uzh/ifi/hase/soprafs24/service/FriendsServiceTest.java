package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.FriendRequestState;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.model.UserModel;
import ch.uzh.ifi.hase.soprafs24.repository.FriendsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

public class FriendsServiceTest {

    @Mock
    private FriendsRepository friendsRepository;

    @Mock
    private UserService userService;
    private User user1;
    private User user2;
    private User user3;

    @InjectMocks
    private FriendsService friendsService;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        user3 = new User();
        user3.setId(3L);
        user3.setUsername("user3");

    }

    @Test
    public void addFriendShouldReturnTrue() {
        when(userService.getUserById(1L)).thenReturn(user1);
        when(userService.getUserById(2L)).thenReturn(user2);
        when(friendsRepository.existsByUser1IdAndUser2IdAndStatus(1L, 2L, FriendRequestState.ACCEPTED)).thenReturn(false);
        when(friendsRepository.findAllPendingRequests(1L)).thenReturn(new ArrayList<>(){{add(user2);}});

        boolean success = friendsService.addFriend(1L, 2L);
        verify(friendsRepository, times(1)).save(any(Friends.class));
        assertTrue(success);
        assertTrue(friendsService.getAllPendingFriendRequests(1L).stream().map(UserModel::getUsername).anyMatch(x -> x.equals(user2.getUsername())));
    }

    @Test
    public void addFriendFriendNull() {
        when(userService.getUserById(1L)).thenReturn(user1);
        when(userService.getUserById(2L)).thenReturn(null);

        assertThrows(ResponseStatusException.class, () -> friendsService.addFriend(1L, 2L));
        verify(friendsRepository, never()).save(any(Friends.class));
    }

    @Test
    public void addFriendUserNull() {
        when(userService.getUserById(1L)).thenReturn(null);
        when(userService.getUserById(2L)).thenReturn(user2);

        assertThrows(ResponseStatusException.class, () -> friendsService.addFriend(1L, 2L));
        verify(friendsRepository, never()).save(any(Friends.class));
    }

    @Test
    public void addFriendAlreadyFriends() {
        when(userService.getUserById(1L)).thenReturn(user1);
        when(userService.getUserById(2L)).thenReturn(user2);
        when(friendsRepository.existsByUser1IdAndUser2IdAndStatus(1L, 2L, FriendRequestState.ACCEPTED)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> friendsService.addFriend(1L, 2L));
        verify(friendsRepository, never()).save(any(Friends.class));
    }

    @Test
    public void acceptFriend() {
        Friends friends = new Friends();
        friends.setUser1(user1);
        friends.setUser2(user2);
        friends.setRequestStatus(FriendRequestState.PENDING);
        when(friendsRepository.findByUser1IdAndUser2IdAndStatus(1L, 2L, FriendRequestState.PENDING)).thenReturn(friends);

        boolean success = friendsService.acceptFriendRequest(1L, 2L);
        verify(friendsRepository, times(1)).save(any(Friends.class));
        assertTrue(success);
        assertEquals(friends.getRequestStatus(), FriendRequestState.ACCEPTED);
    }

    @Test
    public void acceptFriendAlreadyFriends() {
        when(friendsRepository.findByUser1IdAndUser2IdAndStatus(1L, 2L, FriendRequestState.PENDING)).thenReturn(null);

        boolean success = friendsService.acceptFriendRequest(1L, 2L);
        verify(friendsRepository, times(0)).save(any(Friends.class));
        assertFalse(success);
    }

    @Test
    public void denyFriend() {
        Friends friends = new Friends();
        friends.setUser1(user1);
        friends.setUser2(user2);
        friends.setRequestStatus(FriendRequestState.PENDING);
        when(friendsRepository.findByUser1IdAndUser2IdAndStatus(1L, 2L, FriendRequestState.PENDING)).thenReturn(friends);

        boolean success = friendsService.denyFriendRequest(1L, 2L);
        verify(friendsRepository, times(1)).save(any(Friends.class));
        assertTrue(success);
        assertEquals(friends.getRequestStatus(), FriendRequestState.DECLINED);
    }

    @Test
    public void denyFriendAlreadyFriends() {
        when(friendsRepository.findByUser1IdAndUser2IdAndStatus(1L, 2L, FriendRequestState.PENDING)).thenReturn(null);

        boolean success = friendsService.denyFriendRequest(1L, 2L);
        verify(friendsRepository, times(0)).save(any(Friends.class));
        assertFalse(success);
    }

    @Test
    public void getAllFriends() {
        when(friendsRepository.findAllFriends(1L)).thenReturn(new ArrayList<>(){{add(user2); add(user3);}});

        List<UserModel> friends = friendsService.getAllFriends(1L);
        verify(friendsRepository, times(0)).save(any(Friends.class));
        assertEquals(friends.size(), 2);
    }

    @Test
    public void getAllPendingRequests() {
        when(friendsRepository.findAllPendingRequests(1L)).thenReturn(new ArrayList<>(){{add(user2);}});

        List<UserModel> friends = friendsService.getAllPendingFriendRequests(1L);
        verify(friendsRepository, times(0)).save(any(Friends.class));
        assertEquals(friends.size(), 1);
    }

    @Test
    public void getAllNotFriends() {
        when(userService.getAllUsersExceptSelf(1L)).thenReturn(new ArrayList<>(){{add(user2); add(user3);}});
        when(friendsRepository.findAllFriends(1L)).thenReturn(new ArrayList<>(){{add(user2);}});
        when(friendsRepository.findAllPendingRequests(1L)).thenReturn(new ArrayList<>());

        List<UserModel> availableForFriendRequest = friendsService.getAllUsersWhichAreNotFriends(1L);
        verify(friendsRepository, times(0)).save(any(Friends.class));
        assertEquals(availableForFriendRequest.size(), 1);
        assertEquals(availableForFriendRequest.get(0).getUsername(), user3.toUserModel().getUsername());
    }

    @Test
    public void getAllNotFriendsNotFoundAny() {
        when(userService.getAllUsersExceptSelf(1L)).thenReturn(new ArrayList<>(){{add(user2); add(user3);}});
        when(friendsRepository.findAllFriends(1L)).thenReturn(new ArrayList<>(){{add(user2);}});
        when(friendsRepository.findAllPendingRequests(1L)).thenReturn(new ArrayList<>(){{add(user3);}});

        List<UserModel> availableForFriendRequest = friendsService.getAllUsersWhichAreNotFriends(1L);
        verify(friendsRepository, times(0)).save(any(Friends.class));
        assertEquals(availableForFriendRequest.size(), 0);
    }

    @Test
    public void ifFriendsRemoveSuccess() {
        Friends friends = new Friends();
        friends.setUser1(user1);
        friends.setUser2(user2);
        friends.setRequestStatus(FriendRequestState.ACCEPTED);
        when(friendsRepository.findByUser1IdAndUser2IdAndStatus(1L, 2L, FriendRequestState.ACCEPTED)).thenReturn(friends);

        boolean success = friendsService.ifFriendsRemove(1L, 2L);
        verify(friendsRepository, times(1)).delete(any(Friends.class));
        assertTrue(success);
    }

    @Test
    public void ifFriendsRemoveFail() {
        when(friendsRepository.findByUser1IdAndUser2IdAndStatus(1L, 2L, FriendRequestState.ACCEPTED)).thenReturn(null);

        assertThrows(ResponseStatusException.class, () -> friendsService.ifFriendsRemove(1L, 2L));

    }
}
