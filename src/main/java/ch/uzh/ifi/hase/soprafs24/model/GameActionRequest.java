package ch.uzh.ifi.hase.soprafs24.model;

public record GameActionRequest(long sessionId, long userId, long amount) {
}
