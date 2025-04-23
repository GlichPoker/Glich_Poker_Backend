package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.HandRank;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public record EvaluationResult(@JsonProperty("handRank")HandRank handRank, @JsonProperty("highCards")Card[] highCards) implements Comparable<EvaluationResult> {

    @Override
    public int compareTo(EvaluationResult other) {
        // Compare HandRank first
        int rankComparison = -Integer.compare(this.handRank.ordinal(), other.handRank.ordinal());
        if (rankComparison != 0) {
            return rankComparison;
        }

        // Compare high cards
        for (int i = 0; i < this.highCards.length; i++) {
            int cardComparison = this.highCards[i].compareTo(other.highCards[i]);
            if (cardComparison != 0) {
                return cardComparison;
            }
        }

        return 0; // Hands are identical
    }

    @Override
    public String toString() {
        return "EvaluationResult [handRank=" + handRank + ", highCards=" + Arrays.toString(highCards) + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EvaluationResult that = (EvaluationResult) obj;
        return handRank == that.handRank && Arrays.equals(highCards, that.highCards);
    }

    @Override
    public int hashCode() {
        int result = handRank.hashCode();
        result = 31 * result + Arrays.hashCode(highCards);
        return result;
    }
}
