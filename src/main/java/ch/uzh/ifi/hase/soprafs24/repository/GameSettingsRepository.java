package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.GameSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameSettingsRepository extends JpaRepository<GameSettings, Long> {
    Optional<GameSettings> findById(Long id);
}
