package ch.uzh.ifi.hase.soprafs24.model;

public record CreateGameRequest(long userId, GameSettings gameSettings, boolean isPublic) {
}
