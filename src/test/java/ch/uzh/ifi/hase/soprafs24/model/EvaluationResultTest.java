package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.EvaluationRank;
import ch.uzh.ifi.hase.soprafs24.constant.HandRank;
import ch.uzh.ifi.hase.soprafs24.constant.Rank;
import ch.uzh.ifi.hase.soprafs24.constant.Suit;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EvaluationResultTest {

    private List<EvaluationResult> highHands;
    private List<EvaluationResult> lowHands;
    private static int count = 0;

    @BeforeEach
    void setup() {
        count++;
        boolean descending = count <= 1;
        highHands = new ArrayList<>();
        highHands.add(new EvaluationResult(HandRank.ROYALFLUSH, new Card[]{new Card(Rank.ACE, Suit.CLUBS)}, EvaluationRank.FIRST, descending, null));
        highHands.add(new EvaluationResult(HandRank.STRAIGHTFLUSH, new Card[]{new Card(Rank.KING, Suit.CLUBS)}, EvaluationRank.SECOND, descending, null));
        highHands.add(new EvaluationResult(HandRank.FOUROFKIND, new Card[]{new Card(Rank.ACE, Suit.CLUBS), new Card(Rank.KING, Suit.SPADES)}, EvaluationRank.THIRD, descending, null));
        highHands.add(new EvaluationResult(HandRank.FULLHOUSE, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.KING, Suit.CLUBS)}, EvaluationRank.FOURTH, descending, null));
        highHands.add(new EvaluationResult(HandRank.FLUSH, new Card[]{new Card(Rank.ACE, Suit.CLUBS), new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS),new Card(Rank.EIGHT, Suit.CLUBS),new Card(Rank.FIVE, Suit.CLUBS)}, EvaluationRank.FIFTH, descending, null));
        highHands.add(new EvaluationResult(HandRank.STRAIGHT, new Card[]{new Card(Rank.ACE, Suit.CLUBS)}, EvaluationRank.SIXTH, descending, null));
        highHands.add(new EvaluationResult(HandRank.THREEOFKIND, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS)}, EvaluationRank.SEVENTH, descending, null));
        highHands.add(new EvaluationResult(HandRank.TWOPAIR, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS)}, EvaluationRank.EIGHTH, descending, null));
        highHands.add(new EvaluationResult(HandRank.ONEPAIR, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS),new Card(Rank.SEVEN, Suit.CLUBS)}, EvaluationRank.NINETH, descending, null));
        highHands.add(new EvaluationResult(HandRank.HIGHCARD, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS),new Card(Rank.SEVEN, Suit.CLUBS),new Card(Rank.FIVE, Suit.CLUBS)}, EvaluationRank.TENTH, descending, null));

        lowHands = new ArrayList<>();
        lowHands.add(new EvaluationResult(HandRank.ROYALFLUSH, new Card[]{new Card(Rank.ACE, Suit.CLUBS)}, EvaluationRank.FIRST, descending, null));
        lowHands.add(new EvaluationResult(HandRank.STRAIGHTFLUSH, new Card[]{new Card(Rank.QUEEN, Suit.CLUBS)}, EvaluationRank.SECOND, descending, null));
        lowHands.add(new EvaluationResult(HandRank.FOUROFKIND, new Card[]{new Card(Rank.ACE, Suit.CLUBS), new Card(Rank.QUEEN, Suit.SPADES)}, EvaluationRank.THIRD, descending, null));
        lowHands.add(new EvaluationResult(HandRank.FULLHOUSE, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS)}, EvaluationRank.FOURTH, descending, null));
        lowHands.add(new EvaluationResult(HandRank.FLUSH, new Card[]{new Card(Rank.ACE, Suit.CLUBS), new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS),new Card(Rank.EIGHT, Suit.CLUBS),new Card(Rank.FOUR, Suit.CLUBS)},EvaluationRank.FIFTH, descending, null));
        lowHands.add(new EvaluationResult(HandRank.STRAIGHT, new Card[]{new Card(Rank.KING, Suit.CLUBS)}, EvaluationRank.SIXTH, descending, null));
        lowHands.add(new EvaluationResult(HandRank.THREEOFKIND, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.NINE, Suit.CLUBS)}, EvaluationRank.SEVENTH, descending, null));
        lowHands.add(new EvaluationResult(HandRank.TWOPAIR, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.NINE, Suit.CLUBS)}, EvaluationRank.EIGHTH, descending, null));
        lowHands.add(new EvaluationResult(HandRank.ONEPAIR, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS),new Card(Rank.SIX, Suit.CLUBS)}, EvaluationRank.NINETH, descending, null));
        lowHands.add(new EvaluationResult(HandRank.HIGHCARD, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS),new Card(Rank.SEVEN, Suit.CLUBS),new Card(Rank.FOUR, Suit.CLUBS)}, EvaluationRank.TENTH, descending, null));

    }

    @Test
    @Order(1)
    void testCompareTo(){
        for(int i = 0; i < highHands.size(); i++){
            for(int j = i; j < lowHands.size(); j++){
                if(i == 0 && j == 0)assertEquals(0, highHands.get(i).compareTo(lowHands.get(j)));
                else assertEquals(-1, highHands.get(i).compareTo(lowHands.get(j)));
            }
        }
    }

    @Test
    void testCompareToAscending(){
        for(int i = 0; i < highHands.size(); i++){
            for(int j = i; j < lowHands.size(); j++){
                if(i == 0 && j == 0)assertEquals(0, highHands.get(i).compareTo(lowHands.get(j)));
                else assertEquals(1, highHands.get(i).compareTo(lowHands.get(j)));
            }
        }
    }

    @Test
    void testToString(){
        EvaluationResult res = highHands.get(0);
        assertEquals("EvaluationResult [handRank=" + res.handRank() + ", highCards=" + Arrays.toString(res.highCards()) + "]", res.toString());
    }

    @Test
    void testEquals(){
        EvaluationResult res = highHands.get(0);
        EvaluationResult res2 = lowHands.get(0);
        assertEquals(res, res2);
    }
    @Test
    void testEqualsNull(){
        EvaluationResult res = highHands.get(0);
        EvaluationResult res2 = null;
        assertNotEquals(res, res2);
    }

    @Test
    void testEqualsSelf(){
        EvaluationResult res = highHands.get(0);
        assertEquals(res, res);
    }

    @Test
    void testHashCode(){
        EvaluationResult res = highHands.get(0);
        assertEquals(res.hashCode(), highHands.get(0).hashCode());
    }
}
