package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.HandRank;
import ch.uzh.ifi.hase.soprafs24.constant.Rank;
import ch.uzh.ifi.hase.soprafs24.constant.Suit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;

class HandEvaluatorTest {

    @Test
    void testRoyalFlush() {
        ArrayList<Card> royalFlushCards = new ArrayList<>(Arrays.asList(
                new Card(Rank.TEN, Suit.SPADES),
                new Card(Rank.JACK, Suit.SPADES),
                new Card(Rank.QUEEN, Suit.SPADES),
                new Card(Rank.KING, Suit.SPADES),
                new Card(Rank.ACE, Suit.SPADES)
        ));

        EvaluationResult result = HandEvaluator.evaluateHand(royalFlushCards);

        assertEquals(HandRank.ROYALFLUSH, result.getHandRank());

        assertEquals(Rank.ACE, result.getHighCards()[0].getRank());
    }

    @Test
    void testStraightFlush() {
        ArrayList<Card> royalFlushCards = new ArrayList<>(Arrays.asList(
                new Card(Rank.TEN, Suit.SPADES),
                new Card(Rank.JACK, Suit.SPADES),
                new Card(Rank.QUEEN, Suit.SPADES),
                new Card(Rank.KING, Suit.SPADES),
                new Card(Rank.NINE, Suit.SPADES),
                new Card(Rank.EIGHT, Suit.SPADES),
                new Card(Rank.SEVEN, Suit.SPADES)
        ));

        EvaluationResult result = HandEvaluator.evaluateHand(royalFlushCards);

        assertEquals(HandRank.STRAIGHTFLUSH, result.getHandRank());

        assertEquals(Rank.KING, result.getHighCards()[0].getRank());
    }

    @Test
    void testStraight() {
        ArrayList<Card> straightCards = new ArrayList<>(Arrays.asList(
                new Card(Rank.TWO, Suit.HEARTS),
                new Card(Rank.THREE, Suit.DIAMONDS),
                new Card(Rank.FOUR, Suit.CLUBS),
                new Card(Rank.FIVE, Suit.SPADES),
                new Card(Rank.SIX, Suit.HEARTS),
                new Card(Rank.SEVEN, Suit.HEARTS),
                new Card(Rank.EIGHT, Suit.HEARTS)
        ));

        EvaluationResult result = HandEvaluator.evaluateHand(straightCards);

        assertEquals(HandRank.STRAIGHT, result.getHandRank());

        assertEquals(Rank.EIGHT, result.getHighCards()[0].getRank());
    }

    @Test
    void testFourOfAKind() {
        ArrayList<Card> fourOfAKindCards = new ArrayList<>(Arrays.asList(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.ACE, Suit.SPADES),
                new Card(Rank.ACE, Suit.DIAMONDS),
                new Card(Rank.KING, Suit.SPADES)
        ));

        EvaluationResult result = HandEvaluator.evaluateHand(fourOfAKindCards);

        assertEquals(HandRank.FOUROFKIND, result.getHandRank());

        assertEquals(Rank.ACE, result.getHighCards()[0].getRank());
        assertEquals(Rank.KING, result.getHighCards()[1].getRank());
    }

    @Test
    void testFullHouse() {
        ArrayList<Card> fullHouseCards = new ArrayList<>(Arrays.asList(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.ACE, Suit.SPADES),
                new Card(Rank.KING, Suit.DIAMONDS),
                new Card(Rank.KING, Suit.SPADES),
                new Card(Rank.QUEEN, Suit.SPADES),
                new Card(Rank.QUEEN, Suit.HEARTS)
        ));

        EvaluationResult result = HandEvaluator.evaluateHand(fullHouseCards);

        assertEquals(HandRank.FULLHOUSE, result.getHandRank());

        assertEquals(Rank.ACE, result.getHighCards()[0].getRank());
        assertEquals(Rank.KING, result.getHighCards()[1].getRank());
    }

    @Test
    void testFullHouseMoreBranchAndLineCoverage() {
        ArrayList<Card> fullHouseCards = new ArrayList<>(Arrays.asList(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.KING, Suit.SPADES),
                new Card(Rank.KING, Suit.DIAMONDS),
                new Card(Rank.KING, Suit.SPADES),
                new Card(Rank.QUEEN, Suit.SPADES),
                new Card(Rank.QUEEN, Suit.HEARTS)
        ));

        EvaluationResult result = HandEvaluator.evaluateHand(fullHouseCards);

        assertEquals(HandRank.FULLHOUSE, result.getHandRank());

        assertEquals(Rank.KING, result.getHighCards()[0].getRank());
        assertEquals(Rank.ACE, result.getHighCards()[1].getRank());
    }

    @Test
    void testFullHouseTwoTrips() {
        ArrayList<Card> fullHouseCards = new ArrayList<>(Arrays.asList(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.ACE, Suit.SPADES),
                new Card(Rank.KING, Suit.DIAMONDS),
                new Card(Rank.KING, Suit.SPADES),
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.QUEEN, Suit.HEARTS)
        ));

        EvaluationResult result = HandEvaluator.evaluateHand(fullHouseCards);

        assertEquals(HandRank.FULLHOUSE, result.getHandRank());

        assertEquals(Rank.ACE, result.getHighCards()[0].getRank());
        assertEquals(Rank.KING, result.getHighCards()[1].getRank());
    }

    @Test
    void testFlush() {
        ArrayList<Card> straightCards = new ArrayList<>(Arrays.asList(
                new Card(Rank.TWO, Suit.HEARTS),
                new Card(Rank.THREE, Suit.HEARTS),
                new Card(Rank.TEN, Suit.HEARTS),
                new Card(Rank.FIVE, Suit.HEARTS),
                new Card(Rank.SIX, Suit.HEARTS),
                new Card(Rank.SEVEN, Suit.HEARTS),
                new Card(Rank.EIGHT, Suit.HEARTS)
        ));

        EvaluationResult result = HandEvaluator.evaluateHand(straightCards);

        assertEquals(HandRank.FLUSH, result.getHandRank());

        assertEquals(Rank.TEN, result.getHighCards()[0].getRank());
        assertEquals(Rank.EIGHT, result.getHighCards()[1].getRank());
        assertEquals(Rank.SEVEN, result.getHighCards()[2].getRank());
        assertEquals(Rank.SIX, result.getHighCards()[3].getRank());
        assertEquals(Rank.FIVE, result.getHighCards()[4].getRank());
    }

    @Test
    void testFullThreeOfAKind() {
        ArrayList<Card> fullHouseCards = new ArrayList<>(Arrays.asList(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.ACE, Suit.SPADES),
                new Card(Rank.KING, Suit.DIAMONDS),
                new Card(Rank.TWO, Suit.SPADES),
                new Card(Rank.JACK, Suit.HEARTS),
                new Card(Rank.QUEEN, Suit.HEARTS)
        ));

        EvaluationResult result = HandEvaluator.evaluateHand(fullHouseCards);

        assertEquals(HandRank.THREEOFKIND, result.getHandRank());

        assertEquals(Rank.ACE, result.getHighCards()[0].getRank());
        assertEquals(Rank.KING, result.getHighCards()[1].getRank());
        assertEquals(Rank.QUEEN, result.getHighCards()[2].getRank());
    }

    @Test
    void testTwoPair() {
        ArrayList<Card> fullHouseCards = new ArrayList<>(Arrays.asList(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.THREE, Suit.SPADES),
                new Card(Rank.QUEEN, Suit.DIAMONDS),
                new Card(Rank.KING, Suit.SPADES),
                new Card(Rank.EIGHT, Suit.HEARTS),
                new Card(Rank.QUEEN, Suit.HEARTS)
        ));

        EvaluationResult result = HandEvaluator.evaluateHand(fullHouseCards);

        assertEquals(HandRank.TWOPAIR, result.getHandRank());

        assertEquals(Rank.ACE, result.getHighCards()[0].getRank());
        assertEquals(Rank.QUEEN, result.getHighCards()[1].getRank());
        assertEquals(Rank.KING, result.getHighCards()[2].getRank());
    }

    @Test
    void testPair() {
        ArrayList<Card> fullHouseCards = new ArrayList<>(Arrays.asList(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.THREE, Suit.SPADES),
                new Card(Rank.KING, Suit.DIAMONDS),
                new Card(Rank.JACK, Suit.SPADES),
                new Card(Rank.QUEEN, Suit.SPADES),
                new Card(Rank.SIX, Suit.HEARTS)
        ));

        EvaluationResult result = HandEvaluator.evaluateHand(fullHouseCards);

        assertEquals(HandRank.ONEPAIR, result.getHandRank());

        assertEquals(Rank.ACE, result.getHighCards()[0].getRank());
        assertEquals(Rank.KING, result.getHighCards()[1].getRank());
        assertEquals(Rank.QUEEN, result.getHighCards()[2].getRank());
        assertEquals(Rank.JACK, result.getHighCards()[3].getRank());
    }

    @Test
    void testHighCards() {
        ArrayList<Card> fullHouseCards = new ArrayList<>(Arrays.asList(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.JACK, Suit.HEARTS),
                new Card(Rank.NINE, Suit.SPADES),
                new Card(Rank.TWO, Suit.DIAMONDS),
                new Card(Rank.THREE, Suit.SPADES),
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.QUEEN, Suit.HEARTS)
        ));

        EvaluationResult result = HandEvaluator.evaluateHand(fullHouseCards);

        assertEquals(HandRank.HIGHCARD, result.getHandRank());

        assertEquals(Rank.ACE, result.getHighCards()[0].getRank());
        assertEquals(Rank.KING, result.getHighCards()[1].getRank());
        assertEquals(Rank.QUEEN, result.getHighCards()[2].getRank());
        assertEquals(Rank.JACK, result.getHighCards()[3].getRank());
        assertEquals(Rank.NINE, result.getHighCards()[4].getRank());
    }
}
