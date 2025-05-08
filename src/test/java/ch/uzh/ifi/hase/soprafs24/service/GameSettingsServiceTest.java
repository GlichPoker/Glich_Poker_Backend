package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.HandRank;
import ch.uzh.ifi.hase.soprafs24.constant.WeatherType;
import ch.uzh.ifi.hase.soprafs24.entity.*;

import ch.uzh.ifi.hase.soprafs24.repository.GameSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

public class GameSettingsServiceTest {

    @Mock
    private GameSettingsRepository settingsRepository;

    @InjectMocks
    private GameSettingsService gameSettingsService;

    private GameSettings gameSettings;
    private ch.uzh.ifi.hase.soprafs24.model.GameSettings otherSettings;



    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        List<HandRank> order = new ArrayList<>(Arrays.stream(HandRank.values()).sorted(Comparator.reverseOrder()).toList());
        gameSettings = new GameSettings();
        gameSettings.setInitialBalance(100);
        gameSettings.setBigBlind(10);
        gameSettings.setSmallBlind(5);
        gameSettings.setOrder(order);
        gameSettings.setDescending(true);

        otherSettings = new ch.uzh.ifi.hase.soprafs24.model.GameSettings(gameSettings);
    }

    @Test
    public void getSettingsFound() {
        when(settingsRepository.findById(1L)).thenReturn(Optional.of(gameSettings));

        GameSettings settings = gameSettingsService.getGameSettings(1L);

        assertEquals(gameSettings, settings);
    }

    @Test
    public void getSettingsNotFound() {
        when(settingsRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> gameSettingsService.getGameSettings(1L));
    }

    @Test
    public void createSettings() {
        when(settingsRepository.save(any(GameSettings.class))).thenReturn(gameSettings);

        GameSettings newSettings = gameSettingsService.createGameSettings(otherSettings);

        verify(settingsRepository, times(1)).save(any(GameSettings.class));
        assertEquals(newSettings.getBigBlind(), gameSettings.getBigBlind());
        assertEquals(newSettings.getSmallBlind(), gameSettings.getSmallBlind());
        assertEquals(newSettings.getInitialBalance(), gameSettings.getInitialBalance());
    }

    @Test
    public void deleteSettings() {
        gameSettingsService.deleteSettings(gameSettings);
        verify(settingsRepository, times(1)).delete(any(GameSettings.class));
    }


    @Test
    public void updateSettings() {
        when(settingsRepository.save(any(GameSettings.class))).thenReturn(gameSettings);

        ch.uzh.ifi.hase.soprafs24.model.GameSettings model = new ch.uzh.ifi.hase.soprafs24.model.GameSettings(1000, 10, 20, null, true, WeatherType.RAINY, "");
        GameSettings newSettings = gameSettingsService.updateSettings(gameSettings, model);
        verify(settingsRepository, times(1)).save(any(GameSettings.class));
        assertEquals(newSettings.getBigBlind(), model.bigBlind());
        assertEquals(newSettings.getSmallBlind(), model.smallBlind());
        assertEquals(newSettings.getInitialBalance(), model.initialBalance());
    }
}