package ch.uzh.ifi.hase.soprafs24.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SwapCardRequest(
        @JsonProperty("sessionId") long sessionId,
        @JsonProperty("userId") long userId,
        @JsonProperty("card") Card card) {
}
