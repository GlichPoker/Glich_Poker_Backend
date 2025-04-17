package ch.uzh.ifi.hase.soprafs24.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ModifyGameSettingsRequest(
        @JsonProperty("sessionId") long sessionId,
        @JsonProperty("gameSettings") GameSettings gameSettings) {
}
