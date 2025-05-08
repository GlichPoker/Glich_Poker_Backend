package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.UserModel;
import ch.uzh.ifi.hase.soprafs24.service.FriendsService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FriendsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FriendsService friendsService;

    @MockBean
    private UserService userService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
    }

    @Test
    void addFriendShouldReturnCreated() throws Exception {
        when(userService.getUserById(1L)).thenReturn(user1);
        when(userService.getUserById(2L)).thenReturn(user2);
        when(friendsService.addFriend(1L, 2L)).thenReturn(true);

        mockMvc.perform(post("/friends/add")
                        .param("userId", "1")
                        .param("friendId", "2"))
                .andExpect(status().isCreated());
    }

    @Test
    void acceptFriendRequestShouldReturnOk() throws Exception {
        when(userService.getUserById(1L)).thenReturn(user1);
        when(userService.getUserById(2L)).thenReturn(user2);
        when(friendsService.acceptFriendRequest(1L, 2L)).thenReturn(true);

        mockMvc.perform(post("/friends/accept")
                        .param("userId", "1")
                        .param("friendId", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void denyFriendRequestShouldReturnOk() throws Exception {
        when(userService.getUserById(1L)).thenReturn(user1);
        when(userService.getUserById(2L)).thenReturn(user2);
        when(friendsService.denyFriendRequest(1L, 2L)).thenReturn(true);

        mockMvc.perform(post("/friends/deny")
                        .param("userId", "1")
                        .param("friendId", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllFriendsShouldReturnOk() throws Exception {
        List<User> friends = Arrays.asList(user2);
        when(userService.getUserById(1L)).thenReturn(user1);
        when(friendsService.getAllFriends(1L)).thenReturn(convertUsersToModels(friends));

        mockMvc.perform(get("/friends/allFriends/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("user2"));
    }

    @Test
    void getAllPendingRequestsShouldReturnOk() throws Exception {
        List<User> pendingRequests = Arrays.asList(user2);
        when(userService.getUserById(1L)).thenReturn(user1);
        when(friendsService.getAllPendingFriendRequests(1L)).thenReturn(convertUsersToModels(pendingRequests));

        mockMvc.perform(get("/friends/pendingRequests/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("user2"));
    }

    @Test
    void getAllFriendsWhichCanBeAddedShouldReturnOk() throws Exception {
        List<User> availableUsers = Arrays.asList(user2);
        when(userService.getUserById(1L)).thenReturn(user1);
        when(friendsService.getAllUsersWhichAreNotFriends(1L)).thenReturn(convertUsersToModels(availableUsers));

        mockMvc.perform(get("/friends/availableUsers/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("user2"));
    }

    @Test
    void removeFriendShouldReturnOk() throws Exception {
        when(userService.getUserById(1L)).thenReturn(user1);
        when(userService.getUserById(2L)).thenReturn(user2);
        when(friendsService.ifFriendsRemove(1L, 2L)).thenReturn(true);

        mockMvc.perform(post("/friends/remove")
                        .param("userId", "1")
                        .param("friendId", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void checkUserNotFoundShouldReturnNotFound() throws Exception {
        when(userService.getUserById(1L)).thenReturn(null);

        mockMvc.perform(post("/friends/add")
                        .param("userId", "1")
                        .param("friendId", "2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkFriendNotFoundShouldReturnNotFound() throws Exception {
        when(userService.getUserById(1L)).thenReturn(user1);
        when(userService.getUserById(2L)).thenReturn(null);

        mockMvc.perform(post("/friends/add")
                        .param("userId", "1")
                        .param("friendId", "2"))
                .andExpect(status().isNotFound());
    }

    private List<UserModel> convertUsersToModels(List<User> users) {
        return users.stream().map(User::toUserModel).toList();
    }
}
