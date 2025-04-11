package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.Rank;
import ch.uzh.ifi.hase.soprafs24.constant.Suit;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record Card(Rank rank, Suit suit) implements Comparable<Card> {

    @Override
    public int compareTo(Card o) {
        return -this.rank.compareTo(o.rank);
    }

    @Override
    public String toString() {
        return suit + ":" + rank;
    }
}
