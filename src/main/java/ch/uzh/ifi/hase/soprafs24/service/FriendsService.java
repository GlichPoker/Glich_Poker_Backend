package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.FriendRequestState;
import ch.uzh.ifi.hase.soprafs24.entity.Friends;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.UserModel;
import ch.uzh.ifi.hase.soprafs24.repository.FriendsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        User user2 = userService.getUserById(friendId);
        if (user == null || user2 == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One of the two users was not found");
        }

        if (friendsRepository.existsByUser1IdAndUser2IdAndStatus(userId, friendId, FriendRequestState.ACCEPTED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Users are already friends");
        }

        // Create a new friend request
        Friends newFriendship = new Friends();
        newFriendship.setUser1(user);
        newFriendship.setUser2(user2);
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

    public List<UserModel> getAllFriends(long userId) {
        return convertUsersToModels(friendsRepository.findAllFriends(userId));
    }

    public List<UserModel> getAllPendingFriendRequests(long userId) {
        return convertUsersToModels(friendsRepository.findAllPendingRequests(userId));
    }

    public List<UserModel> getAllUsersWhichAreNotFriends(long userId) {
        List<User> allUsers = userService.getAllUsersExceptSelf(userId);
        Set<Long> allFriends = new HashSet<>(friendsRepository.findAllFriends(userId).stream().map(User::getId).toList());
        allFriends.addAll(friendsRepository.findAllPendingRequests(userId).stream().map(User::getId).toList());
        List<User> result = new ArrayList<>();
        for (User user : allUsers) {
            if (!allFriends.contains(user.getId())) {
                result.add(user);
            }
        }
        return convertUsersToModels(result);
    }

    public boolean ifFriendsRemove(long userId, long friendId) {
        Friends friendship = friendsRepository.findByUser1IdAndUser2IdAndStatus(userId, friendId, FriendRequestState.ACCEPTED);
        if (friendship == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friendship not found");

        friendsRepository.delete(friendship);
        friendsRepository.flush();
        return true;
    }

    private List<UserModel> convertUsersToModels(List<User> users) {
        return users.stream().map(User::toUserModel).toList();
    }
}
