package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.AllowedUser;
import ch.uzh.ifi.hase.soprafs24.entity.AllowedUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("allowedUserRepository")
public interface AllowedUserRepository extends JpaRepository<AllowedUser, AllowedUserId> {
    // Find if a specific user is allowed in a specific game
    Optional<AllowedUser> findById_GameIdAndId_UserId(Long gameId, Long userId);

    // Find all users allowed for a game
    List<AllowedUser> findById_GameId(Long gameId);

    // Find all games a user is allowed in
    List<AllowedUser> findById_UserId(Long userId);
}