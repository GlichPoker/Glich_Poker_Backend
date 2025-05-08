package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.HandRank;
import ch.uzh.ifi.hase.soprafs24.constant.Rank;
import ch.uzh.ifi.hase.soprafs24.constant.Suit;
import ch.uzh.ifi.hase.soprafs24.constant.WeatherType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class HandEvaluatorTest {
    private static GameSettings settings;
    @BeforeAll
    static void setup() {
        List<HandRank> order = new ArrayList<>(Arrays.stream(HandRank.values()).sorted(Comparator.reverseOrder()).toList());
        settings = new GameSettings(1000,10,20, order, true, WeatherType.CLOUDY, "");
    }

    @Test
    void testRoyalFlush() {
        ArrayList<Card> royalFlushCards = new ArrayList<>(Arrays.asList(
                new Card(Rank.TEN, Suit.SPADES),
                new Card(Rank.JACK, Suit.SPADES),
                new Card(Rank.QUEEN, Suit.SPADES),
                new Card(Rank.KING, Suit.SPADES),
                new Card(Rank.ACE, Suit.SPADES)
        ));

        EvaluationResult result = HandEvaluator.evaluateHand(royalFlushCards, settings);

        assertEquals(HandRank.ROYALFLUSH, result.handRank());

        assertEquals(Rank.ACE, result.highCards()[0].rank());
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

        EvaluationResult result = HandEvaluator.evaluateHand(royalFlushCards, settings);

        assertEquals(HandRank.STRAIGHTFLUSH, result.handRank());

        assertEquals(Rank.KING, result.highCards()[0].rank());
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

        EvaluationResult result = HandEvaluator.evaluateHand(straightCards, settings);

        assertEquals(HandRank.STRAIGHT, result.handRank());

        assertEquals(Rank.EIGHT, result.highCards()[0].rank());
    }

    @Test
    void testStraightWithDuplicate() {
        ArrayList<Card> straightCards = new ArrayList<>(Arrays.asList(
                new Card(Rank.THREE, Suit.HEARTS),
                new Card(Rank.FOUR, Suit.DIAMONDS),
                new Card(Rank.FIVE, Suit.CLUBS),
                new Card(Rank.SIX, Suit.SPADES),
                new Card(Rank.SEVEN, Suit.HEARTS),
                new Card(Rank.SEVEN, Suit.HEARTS),
                new Card(Rank.EIGHT, Suit.HEARTS)
        ));

        EvaluationResult result = HandEvaluator.evaluateHand(straightCards, settings);

        assertEquals(HandRank.STRAIGHT, result.handRank());

        assertEquals(Rank.EIGHT, result.highCards()[0].rank());
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

        EvaluationResult result = HandEvaluator.evaluateHand(fourOfAKindCards, settings);

        assertEquals(HandRank.FOUROFKIND, result.handRank());

        assertEquals(Rank.ACE, result.highCards()[0].rank());
        assertEquals(Rank.KING, result.highCards()[1].rank());
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

        EvaluationResult result = HandEvaluator.evaluateHand(fullHouseCards, settings);

        assertEquals(HandRank.FULLHOUSE, result.handRank());

        assertEquals(Rank.ACE, result.highCards()[0].rank());
        assertEquals(Rank.KING, result.highCards()[1].rank());
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

        EvaluationResult result = HandEvaluator.evaluateHand(fullHouseCards, settings);

        assertEquals(HandRank.FULLHOUSE, result.handRank());

        assertEquals(Rank.KING, result.highCards()[0].rank());
        assertEquals(Rank.ACE, result.highCards()[1].rank());
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

        EvaluationResult result = HandEvaluator.evaluateHand(fullHouseCards, settings);

        assertEquals(HandRank.FULLHOUSE, result.handRank());

        assertEquals(Rank.ACE, result.highCards()[0].rank());
        assertEquals(Rank.KING, result.highCards()[1].rank());
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

        EvaluationResult result = HandEvaluator.evaluateHand(straightCards, settings);

        assertEquals(HandRank.FLUSH, result.handRank());

        assertEquals(Rank.TEN, result.highCards()[0].rank());
        assertEquals(Rank.EIGHT, result.highCards()[1].rank());
        assertEquals(Rank.SEVEN, result.highCards()[2].rank());
        assertEquals(Rank.SIX, result.highCards()[3].rank());
        assertEquals(Rank.FIVE, result.highCards()[4].rank());
    }

    @Test
    void testThreeOfAKind() {
        ArrayList<Card> fullHouseCards = new ArrayList<>(Arrays.asList(
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.ACE, Suit.SPADES),
                new Card(Rank.KING, Suit.DIAMONDS),
                new Card(Rank.TWO, Suit.SPADES),
                new Card(Rank.JACK, Suit.HEARTS),
                new Card(Rank.QUEEN, Suit.HEARTS)
        ));

        EvaluationResult result = HandEvaluator.evaluateHand(fullHouseCards, settings);

        assertEquals(HandRank.THREEOFKIND, result.handRank());

        assertEquals(Rank.ACE, result.highCards()[0].rank());
        assertEquals(Rank.KING, result.highCards()[1].rank());
        assertEquals(Rank.QUEEN, result.highCards()[2].rank());
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

        EvaluationResult result = HandEvaluator.evaluateHand(fullHouseCards, settings);

        assertEquals(HandRank.TWOPAIR, result.handRank());

        assertEquals(Rank.ACE, result.highCards()[0].rank());
        assertEquals(Rank.QUEEN, result.highCards()[1].rank());
        assertEquals(Rank.KING, result.highCards()[2].rank());
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

        EvaluationResult result = HandEvaluator.evaluateHand(fullHouseCards, settings);

        assertEquals(HandRank.ONEPAIR, result.handRank());

        assertEquals(Rank.ACE, result.highCards()[0].rank());
        assertEquals(Rank.KING, result.highCards()[1].rank());
        assertEquals(Rank.QUEEN, result.highCards()[2].rank());
        assertEquals(Rank.JACK, result.highCards()[3].rank());
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

        EvaluationResult result = HandEvaluator.evaluateHand(fullHouseCards, settings);

        assertEquals(HandRank.HIGHCARD, result.handRank());

        assertEquals(Rank.ACE, result.highCards()[0].rank());
        assertEquals(Rank.KING, result.highCards()[1].rank());
        assertEquals(Rank.QUEEN, result.highCards()[2].rank());
        assertEquals(Rank.JACK, result.highCards()[3].rank());
        assertEquals(Rank.NINE, result.highCards()[4].rank());
    }
}
