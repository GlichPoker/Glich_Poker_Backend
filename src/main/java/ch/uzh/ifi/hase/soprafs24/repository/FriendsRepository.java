package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.constant.FriendRequestState;
import ch.uzh.ifi.hase.soprafs24.entity.Friends;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository("friendsRepository")
public interface FriendsRepository extends JpaRepository<Friends, Long> {
    List<Friends> findAll();
    @Query(value = "SELECT u FROM User u " +
            "JOIN Friends f ON u.id = f.user1.id OR u.id = f.user2.id " +
            "WHERE (f.user1.id = :userId OR f.user2.id = :userId) " +
            "AND f.status = 1 " +
            "AND u.id <> :userId")
    List<User> findAllFriends(@Param("userId") long userId);
    @Query(value = "SELECT u FROM User u " +
            "JOIN Friends f ON u.id = f.user1.id OR u.id = f.user2.id " +
            "WHERE (f.user1.id = :userId OR f.user2.id = :userId) " +
            "AND f.status = 0 " +
            "AND u.id <> :userId")
    List<User> findAllPendingRequests(@Param("userId") long userId);
    @Query(value = "SELECT u FROM User u " +
            "JOIN Friends f ON u.id = f.user1.id OR u.id = f.user2.id " +
            "WHERE (f.user2.id = :userId ) " +
            "AND f.status = 0 " +
            "AND u.id <> :userId")
    List<User> findAllPendingRequestsUniDirectional(@Param("userId") long userId);
    @Query("SELECT f FROM Friends f " +
            "WHERE (f.user1.id = :userId AND f.user2.id = :friendId OR f.user1.id = :friendId AND f.user2.id = :userId)" +
            " AND f.status = :status")
    Friends findByUser1IdAndUser2IdAndStatus(@Param("userId") Long userId, @Param("friendId") Long friendId, @Param("status") FriendRequestState status);

    @Query("SELECT COUNT(f) > 0 FROM Friends f " +
            "WHERE (f.user1.id = :userId AND f.user2.id = :friendId OR f.user1.id = :friendId AND f.user2.id = :userId) " +
            "AND f.status = :status")
    boolean existsByUser1IdAndUser2IdAndStatus(@Param("userId") Long userId, @Param("friendId") Long friendId, @Param("status") FriendRequestState status);
}
