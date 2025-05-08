package ch.uzh.ifi.hase.soprafs24.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateGameRequest(
                @JsonProperty("userId") long userId,
                @JsonProperty("gameSettings") GameSettings gameSettings,
                @JsonProperty("isPublic") boolean isPublic) {
}