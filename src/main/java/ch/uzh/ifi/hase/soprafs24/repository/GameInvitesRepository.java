package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Invite;
import ch.uzh.ifi.hase.soprafs24.entity.InviteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("allowedUserRepository")
public interface GameInvitesRepository extends JpaRepository<Invite, InviteId> {
    // Find if a specific user is allowed in a specific game
    Optional<Invite> findById_GameIdAndId_UserId(Long gameId, Long userId);

    // Find all users allowed for a game
    List<Invite> findById_GameId(Long gameId);

    // Find all games a user is allowed in
    List<Invite> findById_UserId(Long userId);
}