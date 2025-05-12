package ch.uzh.ifi.hase.soprafs24.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Rank {
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    JACK(11),
    QUEEN(12),
    KING(13),
    ACE(14);

    public final int value;

    Rank(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Rank fromValue(int value) {
        for (Rank rank : Rank.values()) {
            if (rank.value == value) {
                return rank;
            }
        }
        throw new IllegalArgumentException("Invalid rank value: " + value);
    }
}
