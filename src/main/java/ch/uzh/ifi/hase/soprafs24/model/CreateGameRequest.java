package ch.uzh.ifi.hase.soprafs24.model;

public class CreateGameRequest {
    private final long userId;
    private final GameSettings gameSettings;

    public CreateGameRequest(long userId, GameSettings gameSettings) {
        this.userId = userId;
        this.gameSettings = gameSettings;
    }

    public long getUserId() { return userId; }

    public GameSettings getGameSettings() { return gameSettings; }
}
