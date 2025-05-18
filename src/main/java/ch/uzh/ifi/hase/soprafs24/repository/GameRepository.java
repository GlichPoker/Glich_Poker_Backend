package ch.uzh.ifi.hase.soprafs24.repository;
import ch.uzh.ifi.hase.soprafs24.entity.User;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findById(long id);
    List<Game> getGamesByOwnerId(long id);
    List<Game> findAll();

    @Query("SELECT DISTINCT g FROM Game g JOIN g.players p WHERE p.user = :user")
    List<Game> findActiveGamesByUser(@Param("user") User user);
}
