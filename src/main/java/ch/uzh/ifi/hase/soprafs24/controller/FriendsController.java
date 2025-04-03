package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.service.FriendsService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/friends")
public class FriendsController {

    private final FriendsService friendshipService;
    private final UserService userService;

    @Autowired
    public FriendsController(FriendsService friendshipService, UserService userService) {
        this.friendshipService = friendshipService;
        this.userService = userService;
    }
    // Endpoint to add a friend request
    @PostMapping("/addFriend")
    @ResponseStatus(HttpStatus.CREATED)
    public boolean addFriend(@RequestParam Long userId, @RequestParam Long friendId) {
        return friendshipService.addFriend(userId, friendId);
    }

    // Endpoint to accept a friend request
    @PostMapping("/acceptFriendRequest")
    @ResponseStatus(HttpStatus.OK)
    public boolean acceptFriendRequest(@RequestParam Long userId, @RequestParam Long friendId) {
        return friendshipService.acceptFriendRequest(userId, friendId);
    }

    // Endpoint to deny a friend request
    @PostMapping("/denyFriendRequest")
    @ResponseStatus(HttpStatus.OK)
    public boolean denyFriendRequest(@RequestParam Long userId, @RequestParam Long friendId) {
        return friendshipService.denyFriendRequest(userId, friendId);
    }

    @GetMapping("/getAllFriends/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<User> getAllFriends(@PathVariable("userId") Long userId) {
        User user = userService.getUserById(userId);
        return friendshipService.getAllFriends(userId);
    }

    @GetMapping("/getAllPendingRequests/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<User> getAllPendingRequests(@PathVariable("userId") Long userId) {
        User user = userService.getUserById(userId);
        return friendshipService.getAllPendingFriendRequests(userId);
    }
}
