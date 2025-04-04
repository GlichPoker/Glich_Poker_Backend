package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.GameSettings;
import ch.uzh.ifi.hase.soprafs24.repository.GameSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GameSettingsService {
    private final GameSettingsRepository gameSettingsRepository;

    @Autowired
    public GameSettingsService(GameSettingsRepository gameSettingsRepository) {
        this.gameSettingsRepository = gameSettingsRepository;
    }

    public GameSettings getGameSettings(long id) {
        Optional<GameSettings> settings = gameSettingsRepository.findById(id);
        if (settings.isPresent()) {
            return settings.get();
        }
        throw new IllegalArgumentException("Game settings with id " + id + " not found");
    }

    public GameSettings createGameSettings(ch.uzh.ifi.hase.soprafs24.model.GameSettings gameSettings) {
        GameSettings newGameSettings = new GameSettings();
        newGameSettings.setBigBlind(gameSettings.initialBalance());
        newGameSettings.setSmallBlind(gameSettings.smallBlind());
        newGameSettings.setBigBlind(gameSettings.bigBlind());
        GameSettings newSettings =  gameSettingsRepository.save(newGameSettings);
        gameSettingsRepository.flush();
        return newSettings;
    }

    public void deleteSettings(GameSettings gameSettings)
    {
        gameSettingsRepository.delete(gameSettings);
        gameSettingsRepository.flush();
    }
}

