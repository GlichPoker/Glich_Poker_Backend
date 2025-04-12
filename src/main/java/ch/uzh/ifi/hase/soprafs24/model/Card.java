package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.Rank;
import ch.uzh.ifi.hase.soprafs24.constant.Suit;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record Card(Rank rank, Suit suit) implements Comparable<Card> {

    public String cardCode() {
        String suitCode = suit.name().substring(0, 1).toUpperCase();

        String rankCode;
        if (rank.value >= 0 && rank.value <= 7) {
            rankCode = String.valueOf(rank.value + 2);
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
