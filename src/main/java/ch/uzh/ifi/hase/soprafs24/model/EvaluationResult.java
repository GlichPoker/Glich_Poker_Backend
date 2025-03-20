package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.HandRank;

import java.util.Arrays;

public class EvaluationResult implements Comparable<EvaluationResult> {
    private final HandRank handRank;
    private final Card[] highCards;

    public EvaluationResult(HandRank handRank, Card[] highCards) {
        this.handRank = handRank;
        this.highCards = highCards;
    }

    public HandRank getHandRank() {
        return handRank;
    }

    public Card[] getHighCards() {
        return highCards;
    }

    @Override
    public int compareTo(EvaluationResult other) {
        // Compare HandRank first
        int rankComparison = -Integer.compare(this.handRank.ordinal(), other.handRank.ordinal());
        if (rankComparison != 0) {
            return rankComparison;
        }

        // Compare high cards lexicographically (assumes Card implements Comparable)
        for (int i = 0; i < this.highCards.length; i++) {
            int cardComparison = -this.highCards[i].compareTo(other.highCards[i]);
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
}
