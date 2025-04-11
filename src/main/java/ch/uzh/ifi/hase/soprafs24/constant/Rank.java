package ch.uzh.ifi.hase.soprafs24.constant;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Rank {
    TWO(0),
    THREE(1),
    FOUR(2),
    FIVE(3),
    SIX(4),
    SEVEN(5),
    EIGHT(6),
    NINE(7),
    TEN(8),
    JACK(9),
    QUEEN(10),
    KING(11),
    ACE(12);

    public final int value;

    Rank(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}
