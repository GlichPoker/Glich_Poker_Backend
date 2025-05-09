package ch.uzh.ifi.hase.soprafs24.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BluffModel(
        @JsonProperty("userId") long userId,
        @JsonProperty("bluffCard") Card bluffCard) {
}