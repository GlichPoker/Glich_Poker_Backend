package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.Rank;
import ch.uzh.ifi.hase.soprafs24.constant.Suit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CardTest {

    private static Card[] testCards;
    @BeforeAll
    static void setup(){
        testCards = new Card[3];
        testCards[0] =new Card(Rank.FOUR, Suit.CLUBS);
        testCards[1] = new Card(Rank.TEN, Suit.HEARTS);
        testCards[2] = new Card(Rank.ACE, Suit.CLUBS);
    }

    @Test
    void testCardCodeNumber(){
        assertEquals("4C", testCards[0].cardCode());
    }

    @Test
    void testCardCodeTen(){
        assertEquals("0H", testCards[1].cardCode());

    }

    @Test
    void testCardCodeFaced(){
        assertEquals("AC", testCards[2].cardCode());

    }

    @Test
    void testCompareTo(){

        assertTrue(testCards[0].compareTo(testCards[1]) > 0);
    }

    @Test
    void testToString(){
        assertEquals("CLUBS:FOUR", testCards[0].toString());
    }
}
