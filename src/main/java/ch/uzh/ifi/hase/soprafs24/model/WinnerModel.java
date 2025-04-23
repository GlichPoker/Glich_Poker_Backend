package ch.uzh.ifi.hase.soprafs24.model;

import java.util.Map;

public class WinnerModel extends RoundModel {
    private final Map<Long, Double> winnings;
    public WinnerModel(Round round, long userId, Map<Long, Double> winnings) {
        super(round, userId);
        this.winnings = winnings;
    }
    public Map<Long, Double> getWinnings() {return winnings;}
}
