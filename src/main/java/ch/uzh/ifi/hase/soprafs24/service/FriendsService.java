package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.FriendRequestState;
import ch.uzh.ifi.hase.soprafs24.entity.Friends;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.FriendsRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendsService {
    private final FriendsRepository friendsRepository;

    private final UserRepository userRepository;

    @Autowired
    public FriendsService(FriendsRepository friendsRepository, UserRepository userRepository) {
        this.friendsRepository = friendsRepository;
        this.userRepository = userRepository;
    }

    public boolean addFriend(Long userId, Long friendId) {
        // Check if user already exists
        if (!userRepository.existsById(userId) || !userRepository.existsById(friendId)) {
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
}
