package ch.uzh.ifi.hase.soprafs24.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GameActionRequest(
        @JsonProperty("sessionId") long sessionId,
        @JsonProperty("userId") long userId,
        @JsonProperty("amount") long amount) {
}
