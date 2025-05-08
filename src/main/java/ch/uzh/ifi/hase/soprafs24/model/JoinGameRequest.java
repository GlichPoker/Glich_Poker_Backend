package ch.uzh.ifi.hase.soprafs24.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record JoinGameRequest(
        @JsonProperty("sessionId") long sessionId,
        @JsonProperty("userId") long userId,
        @JsonProperty("password") String password) {
}
