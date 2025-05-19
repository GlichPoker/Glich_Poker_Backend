package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    @Query("SELECT p from Player p where p.user.id = :userId and p.game.id = :gameId")
    Optional<Player> findByUserAndGameId(@Param("userId") long userId, @Param("gameId") long gameId);
    List<Player> findByGameId(long gameId);
    List<Player> findByUserId(long userId);
}
