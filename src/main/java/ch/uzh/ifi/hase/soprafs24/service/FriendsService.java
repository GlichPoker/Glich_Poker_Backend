package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.FriendRequestState;
import ch.uzh.ifi.hase.soprafs24.entity.Friends;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.FriendsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FriendsService {
    private final FriendsRepository friendsRepository;

    private final UserService userService;

    @Autowired
    public FriendsService(FriendsRepository friendsRepository, UserService userService) {
        this.friendsRepository = friendsRepository;
        this.userService = userService;
    }

    public boolean addFriend(Long userId, Long friendId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Check if the users are already friends (if they exist in the friends table)
        if (friendsRepository.existsByUser1IdAndUser2IdAndStatus(userId, friendId, FriendRequestState.ACCEPTED) ||
                friendsRepository.existsByUser1IdAndUser2IdAndStatus(friendId, userId, FriendRequestState.ACCEPTED)) {
            return false;  // They are already friends
        }

        // Create a new friend request
        Friends newFriendship = new Friends();
        newFriendship.setUser1Id(userId);
        newFriendship.setUser2Id(friendId);
        newFriendship.setRequestStatus(FriendRequestState.PENDING); // Initial status is 'PENDING'
        friendsRepository.save(newFriendship);

        return true;
    }

    public boolean acceptFriendRequest(Long userId, Long friendId) {
        Friends friendship = friendsRepository.findByUser1IdAndUser2IdAndStatus(userId, friendId, FriendRequestState.PENDING);

        if (friendship != null) {
            friendship.setRequestStatus(FriendRequestState.ACCEPTED); // Change status to ACCEPTED
            friendsRepository.save(friendship);
            return true;
        }
        return false;  // If no pending request found
    }

    // Deny a friend request (change status to REJECTED)
    public boolean denyFriendRequest(Long userId, Long friendId) {
        Friends friendship = friendsRepository.findByUser1IdAndUser2IdAndStatus(userId, friendId, FriendRequestState.PENDING);

        if (friendship != null) {
            friendship.setRequestStatus(FriendRequestState.DECLINED);
            friendsRepository.save(friendship);
            return true;
        }
        return false;
    }

    public List<User> getAllFriends(long userId) {
        return friendsRepository.findAllFriends(userId);
    }

    public List<User> getAllPendingFriendRequests(long userId) {
        return friendsRepository.findAllPendingRequests(userId);
    }

    public List<User> getAllUsersWhichAreNotFriends(long userId) {
        List<User> allUsers = userService.getAllUsersExceptSelf(userId);
        Set<Long> allFriends = new HashSet<>(friendsRepository.findAllFriends(userId).stream().map(User::getId).toList());
        allFriends.addAll(friendsRepository.findAllPendingRequests(userId).stream().map(User::getId).toList());
        List<User> result = new ArrayList<>();
        for (User user : allUsers) {
            if (!allFriends.contains(user.getId())) {
                result.add(user);
            }
        }
        return result;
    }

    public boolean ifFriendsRemove(long userId, long friendId) {
        Friends friendship = friendsRepository.findByUser1IdAndUser2IdAndStatus(userId, friendId, FriendRequestState.ACCEPTED);
        if (friendship == null) throw new IllegalArgumentException("Friendship not found");

        friendsRepository.delete(friendship);
        return true;
    }
}
