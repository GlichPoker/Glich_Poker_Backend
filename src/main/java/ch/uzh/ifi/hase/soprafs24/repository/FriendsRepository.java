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
    @Query(value = "SELECT u.*" +
            "FROM users u " +
            "JOIN friends f ON u.id = f.user1_id OR u.id = f.user2_id " +
            "WHERE (f.user1_id = :userId OR f.user2_id = :userId) " +
            "AND f.status = 1 " +
            "AND u.id <> :userId", nativeQuery = true)
    List<User> findAllFriends(@Param("userId") long userId);
    @Query(value = "SELECT u.*" +
            "FROM users u " +
            "JOIN friends f ON u.id = f.user1_id OR u.id = f.user2_id " +
            "WHERE (f.user1_id = :userId OR f.user2_id = :userId) " +
            "AND f.status = 0 " +
            "AND u.id <> :userId", nativeQuery = true)
    List<User> findAllPendingRequests(@Param("userId") long userId);
    boolean existsByUser1IdAndUser2IdAndStatus(Long user1Id, Long user2Id, FriendRequestState status);
    Friends findByUser1IdAndUser2IdAndStatus(Long user1Id, Long user2Id, FriendRequestState status);




}
