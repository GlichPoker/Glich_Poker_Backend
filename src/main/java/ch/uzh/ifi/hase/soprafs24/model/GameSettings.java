package ch.uzh.ifi.hase.soprafs24.model;

public record GameSettings(long initialBalance, long smallBlind, long bigBlind) {

    public GameSettings(ch.uzh.ifi.hase.soprafs24.entity.GameSettings gameSettings) {
        this(gameSettings.getInitialBalance(), gameSettings.getSmallBlind(), gameSettings.getBigBlind());
    }
}
