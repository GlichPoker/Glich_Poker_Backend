package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.service.FriendsService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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

    private void checkIdExists(long id) {
        User u = userService.getUserById(id);
        if (u == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("User with id %d not found", id));
    }

    private void checkIdsExists(long id, long friendId){
        User u = userService.getUserById(id);
        User u2 = userService.getUserById(friendId);
        if(u == null || u2 == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public boolean addFriend(@RequestParam Long userId, @RequestParam Long friendId) {
        checkIdsExists(userId, friendId);
        return friendshipService.addFriend(userId, friendId);
    }

    @PostMapping("/accept")
    @ResponseStatus(HttpStatus.OK)
    public boolean acceptFriendRequest(@RequestParam Long userId, @RequestParam Long friendId) {
        checkIdsExists(userId, friendId);
        return friendshipService.acceptFriendRequest(userId, friendId);
    }

    @PostMapping("/deny")
    @ResponseStatus(HttpStatus.OK)
    public boolean denyFriendRequest(@RequestParam Long userId, @RequestParam Long friendId) {
        checkIdsExists(userId, friendId);
        return friendshipService.denyFriendRequest(userId, friendId);
    }

    @GetMapping("/allFriends/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<User> getAllFriends(@PathVariable("userId") Long userId) {
        checkIdExists(userId);
        return friendshipService.getAllFriends(userId);
    }

    @GetMapping("/pendingRequests/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<User> getAllPendingRequests(@PathVariable("userId") Long userId) {
        checkIdExists(userId);
        return friendshipService.getAllPendingFriendRequests(userId);
    }

    @GetMapping("/availableUsers/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<User> getAllFriendsWhichCanBeAdded(@PathVariable("userId") Long userId) {
        checkIdExists(userId);
        return friendshipService.getAllUsersWhichAreNotFriends(userId);
    }

    @PostMapping("/remove")
    @ResponseStatus(HttpStatus.OK)
    public boolean removeFriend(@RequestParam Long userId, @RequestParam Long friendId) {
        checkIdsExists(userId, friendId);
        return friendshipService.ifFriendsRemove(userId, friendId);
    }
}
