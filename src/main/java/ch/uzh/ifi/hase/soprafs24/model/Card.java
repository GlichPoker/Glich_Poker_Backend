package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.Rank;
import ch.uzh.ifi.hase.soprafs24.constant.Suit;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Card(@JsonProperty("rank") Rank rank, @JsonProperty("suit") Suit suit) implements Comparable<Card> {
    @JsonProperty("cardCode")
    public String cardCode() {
        String suitCode = suit.name().substring(0, 1).toUpperCase();

        String rankCode;
        if (rank.value >= 2 && rank.value <= 9) {
            rankCode = String.valueOf(rank.value);
        } else if (rank == Rank.TEN) {
            rankCode = "0";
        } else {
            rankCode = rank.name().substring(0, 1).toUpperCase();
        }

        return rankCode + suitCode;
    }

    @Override
    public int compareTo(Card o) {
        return -this.rank.compareTo(o.rank);
    }

    @Override
    public String toString() {
        return suit + ":" + rank;
    }
}
