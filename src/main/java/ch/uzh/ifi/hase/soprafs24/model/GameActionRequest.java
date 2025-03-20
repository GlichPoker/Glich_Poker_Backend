package ch.uzh.ifi.hase.soprafs24.model;

public class GameActionRequest {
    private final long sessionId;
    private final long userId;
    private final long amount;

    public GameActionRequest(long sessionId, long userId, long amount) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.amount = amount;
    }
    public long getSessionId() { return sessionId; }

    public long getUserId() { return userId; }

    public long getAmount() { return amount; }
}
