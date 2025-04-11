package ch.uzh.ifi.hase.soprafs24.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DenyInvitationRequest(
        @JsonProperty("sessionId") long sessionId,
        @JsonProperty("userId") long userId,
        @JsonProperty("requestId") long requestId) {
}
