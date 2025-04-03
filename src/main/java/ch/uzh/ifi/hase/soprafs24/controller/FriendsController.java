package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.service.FriendsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/friends")
public class FriendsController {

    private final FriendsService friendshipService;

    @Autowired
    public FriendsController(FriendsService friendshipService) {
        this.friendshipService = friendshipService;
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
}
