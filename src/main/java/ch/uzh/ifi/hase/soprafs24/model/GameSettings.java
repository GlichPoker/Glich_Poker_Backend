package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.HandRank;
import ch.uzh.ifi.hase.soprafs24.constant.WeatherType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GameSettings(
        @JsonProperty("initialBalance") long initialBalance,
        @JsonProperty("smallBlind") long smallBlind,
        @JsonProperty("bigBlind") long bigBlind,
        @JsonProperty("order") List<HandRank> order,
        @JsonProperty("descending") boolean descending,
        @JsonProperty("weatherType") WeatherType weatherType,
        @JsonProperty("password") String password) {

    public GameSettings(ch.uzh.ifi.hase.soprafs24.entity.GameSettings gameSettings) {
        this(gameSettings.getInitialBalance(), gameSettings.getSmallBlind(), gameSettings.getBigBlind(),
                gameSettings.getOrder(), gameSettings.isDescending(), gameSettings.getWeatherType(),
                gameSettings.getPassword());
    }
}
