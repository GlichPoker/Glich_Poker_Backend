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
public class EvaluationResultTest {

    private List<EvaluationResult> highHands;
    private List<EvaluationResult> lowHands;
    private static int count = 0;

    @BeforeEach
    public void setup() {
        count++;
        boolean descending = count <= 1;
        highHands = new ArrayList<>();
        highHands.add(new EvaluationResult(HandRank.ROYALFLUSH, new Card[]{new Card(Rank.ACE, Suit.CLUBS)}, EvaluationRank.FIRST, descending));
        highHands.add(new EvaluationResult(HandRank.STRAIGHTFLUSH, new Card[]{new Card(Rank.KING, Suit.CLUBS)}, EvaluationRank.SECOND, descending));
        highHands.add(new EvaluationResult(HandRank.FOUROFKIND, new Card[]{new Card(Rank.ACE, Suit.CLUBS), new Card(Rank.KING, Suit.SPADES)}, EvaluationRank.THIRD, descending));
        highHands.add(new EvaluationResult(HandRank.FULLHOUSE, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.KING, Suit.CLUBS)}, EvaluationRank.FOURTH, descending));
        highHands.add(new EvaluationResult(HandRank.FLUSH, new Card[]{new Card(Rank.ACE, Suit.CLUBS), new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS),new Card(Rank.EIGHT, Suit.CLUBS),new Card(Rank.FIVE, Suit.CLUBS)}, EvaluationRank.FIFTH, descending));
        highHands.add(new EvaluationResult(HandRank.STRAIGHT, new Card[]{new Card(Rank.ACE, Suit.CLUBS)}, EvaluationRank.SIXTH, descending));
        highHands.add(new EvaluationResult(HandRank.THREEOFKIND, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS)}, EvaluationRank.SEVENTH, descending));
        highHands.add(new EvaluationResult(HandRank.TWOPAIR, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS)}, EvaluationRank.EIGHTH, descending));
        highHands.add(new EvaluationResult(HandRank.ONEPAIR, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS),new Card(Rank.SEVEN, Suit.CLUBS)}, EvaluationRank.NINETH, descending));
        highHands.add(new EvaluationResult(HandRank.HIGHCARD, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS),new Card(Rank.SEVEN, Suit.CLUBS),new Card(Rank.FIVE, Suit.CLUBS)}, EvaluationRank.TENTH, descending));

        lowHands = new ArrayList<>();
        lowHands.add(new EvaluationResult(HandRank.ROYALFLUSH, new Card[]{new Card(Rank.ACE, Suit.CLUBS)}, EvaluationRank.FIRST, descending));
        lowHands.add(new EvaluationResult(HandRank.STRAIGHTFLUSH, new Card[]{new Card(Rank.QUEEN, Suit.CLUBS)}, EvaluationRank.SECOND, descending));
        lowHands.add(new EvaluationResult(HandRank.FOUROFKIND, new Card[]{new Card(Rank.ACE, Suit.CLUBS), new Card(Rank.QUEEN, Suit.SPADES)}, EvaluationRank.THIRD, descending));
        lowHands.add(new EvaluationResult(HandRank.FULLHOUSE, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS)}, EvaluationRank.FOURTH, descending));
        lowHands.add(new EvaluationResult(HandRank.FLUSH, new Card[]{new Card(Rank.ACE, Suit.CLUBS), new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS),new Card(Rank.EIGHT, Suit.CLUBS),new Card(Rank.FOUR, Suit.CLUBS)},EvaluationRank.FIFTH, descending));
        lowHands.add(new EvaluationResult(HandRank.STRAIGHT, new Card[]{new Card(Rank.KING, Suit.CLUBS)}, EvaluationRank.SIXTH, descending));
        lowHands.add(new EvaluationResult(HandRank.THREEOFKIND, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.NINE, Suit.CLUBS)}, EvaluationRank.SEVENTH, descending));
        lowHands.add(new EvaluationResult(HandRank.TWOPAIR, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.NINE, Suit.CLUBS)}, EvaluationRank.EIGHTH, descending));
        lowHands.add(new EvaluationResult(HandRank.ONEPAIR, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS),new Card(Rank.SIX, Suit.CLUBS)}, EvaluationRank.NINETH, descending));
        lowHands.add(new EvaluationResult(HandRank.HIGHCARD, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS),new Card(Rank.SEVEN, Suit.CLUBS),new Card(Rank.FOUR, Suit.CLUBS)}, EvaluationRank.TENTH, descending));

    }

    @Test
    @Order(1)
    public void testCompareTo(){
        for(int i = 0; i < highHands.size(); i++){
            for(int j = i; j < lowHands.size(); j++){
                if(i == 0 && j == 0)assertEquals(0, highHands.get(i).compareTo(lowHands.get(j)));
                else assertEquals(-1, highHands.get(i).compareTo(lowHands.get(j)));
            }
        }
    }

    @Test
    public void testCompareToAscending(){
        for(int i = 0; i < highHands.size(); i++){
            for(int j = i; j < lowHands.size(); j++){
                if(i == 0 && j == 0)assertEquals(0, highHands.get(i).compareTo(lowHands.get(j)));
                else assertEquals(1, highHands.get(i).compareTo(lowHands.get(j)));
            }
        }
    }

    @Test
    public void testToString(){
        EvaluationResult res = highHands.get(0);
        assertEquals("EvaluationResult [handRank=" + res.handRank() + ", highCards=" + Arrays.toString(res.highCards()) + "]", res.toString());
    }

    @Test
    public void testEquals(){
        EvaluationResult res = highHands.get(0);
        EvaluationResult res2 = lowHands.get(0);
        assertEquals(res, res2);
    }
    @Test
    public void testEqualsNull(){
        EvaluationResult res = highHands.get(0);
        EvaluationResult res2 = null;
        assertNotEquals(res, res2);
    }

    @Test
    public void testEqualsSelf(){
        EvaluationResult res = highHands.get(0);
        assertEquals(res, res);
    }

    @Test
    public void testHashCode(){
        EvaluationResult res = highHands.get(0);
        assertEquals(res.hashCode(), highHands.get(0).hashCode());
    }
}
