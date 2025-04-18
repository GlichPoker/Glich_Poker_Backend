package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.Rank;
import ch.uzh.ifi.hase.soprafs24.constant.Suit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CardTest {

    private static Card[] testCards;
    @BeforeAll
    public static void setup(){
        testCards = new Card[3];
        testCards[0] =new Card(Rank.FOUR, Suit.CLUBS);
        testCards[1] = new Card(Rank.TEN, Suit.HEARTS);
        testCards[2] = new Card(Rank.ACE, Suit.CLUBS);
    }

    @Test
    public void testCardCodeNumber(){
        assertEquals("4C", testCards[0].cardCode());
    }

    @Test
    public void testCardCodeTen(){
        assertEquals("0H", testCards[1].cardCode());

    }

    @Test
    public void testCardCodeFaced(){
        assertEquals("AC", testCards[2].cardCode());

    }

    @Test
    public void testCompareTo(){
        assert(testCards[0].compareTo(testCards[1]) > 0);
    }

    @Test
    public void testToString(){
        assertEquals("CLUBS:FOUR", testCards[0].toString());
    }
}
