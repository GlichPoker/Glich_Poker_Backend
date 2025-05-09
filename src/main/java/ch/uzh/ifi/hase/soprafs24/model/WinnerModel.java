package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.WeatherType;

import java.util.Map;

public class WinnerModel extends RoundModel {
    private final Map<Long, Double> winnings;
    public WinnerModel(Round round, long userId, Map<Long, Double> winnings, WeatherType weatherType) {
        super(round, userId, weatherType);
        this.winnings = winnings;
    }
    public Map<Long, Double> getWinnings() {return winnings;}
}
