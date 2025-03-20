package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.HandRank;
import ch.uzh.ifi.hase.soprafs24.constant.Rank;
import ch.uzh.ifi.hase.soprafs24.constant.Suit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class EvaluationResultTest {

    private List<EvaluationResult> highHands;
    private List<EvaluationResult> lowHands;

    @BeforeEach
    public void setup() {
        highHands = new ArrayList<>();
        highHands.add(new EvaluationResult(HandRank.ROYALFLUSH, new Card[]{new Card(Rank.ACE, Suit.CLUBS)}));
        highHands.add(new EvaluationResult(HandRank.STRAIGHTFLUSH, new Card[]{new Card(Rank.KING, Suit.CLUBS)}));
        highHands.add(new EvaluationResult(HandRank.FOUROFKIND, new Card[]{new Card(Rank.ACE, Suit.CLUBS), new Card(Rank.KING, Suit.SPADES)}));
        highHands.add(new EvaluationResult(HandRank.FULLHOUSE, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.KING, Suit.CLUBS)}));
        highHands.add(new EvaluationResult(HandRank.FLUSH, new Card[]{new Card(Rank.ACE, Suit.CLUBS), new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS),new Card(Rank.EIGHT, Suit.CLUBS),new Card(Rank.FIVE, Suit.CLUBS)}));
        highHands.add(new EvaluationResult(HandRank.STRAIGHT, new Card[]{new Card(Rank.ACE, Suit.CLUBS)}));
        highHands.add(new EvaluationResult(HandRank.THREEOFKIND, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS)}));
        highHands.add(new EvaluationResult(HandRank.TWOPAIR, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS)}));
        highHands.add(new EvaluationResult(HandRank.ONEPAIR, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS),new Card(Rank.SEVEN, Suit.CLUBS)}));
        highHands.add(new EvaluationResult(HandRank.HIGHCARD, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS),new Card(Rank.SEVEN, Suit.CLUBS),new Card(Rank.FIVE, Suit.CLUBS)}));

        lowHands = new ArrayList<>();
        lowHands.add(new EvaluationResult(HandRank.ROYALFLUSH, new Card[]{new Card(Rank.ACE, Suit.CLUBS)}));
        lowHands.add(new EvaluationResult(HandRank.STRAIGHTFLUSH, new Card[]{new Card(Rank.QUEEN, Suit.CLUBS)}));
        lowHands.add(new EvaluationResult(HandRank.FOUROFKIND, new Card[]{new Card(Rank.ACE, Suit.CLUBS), new Card(Rank.QUEEN, Suit.SPADES)}));
        lowHands.add(new EvaluationResult(HandRank.FULLHOUSE, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS)}));
        lowHands.add(new EvaluationResult(HandRank.FLUSH, new Card[]{new Card(Rank.ACE, Suit.CLUBS), new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS),new Card(Rank.EIGHT, Suit.CLUBS),new Card(Rank.FOUR, Suit.CLUBS)}));
        lowHands.add(new EvaluationResult(HandRank.STRAIGHT, new Card[]{new Card(Rank.KING, Suit.CLUBS)}));
        lowHands.add(new EvaluationResult(HandRank.THREEOFKIND, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.NINE, Suit.CLUBS)}));
        lowHands.add(new EvaluationResult(HandRank.TWOPAIR, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.NINE, Suit.CLUBS)}));
        lowHands.add(new EvaluationResult(HandRank.ONEPAIR, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS),new Card(Rank.SIX, Suit.CLUBS)}));
        lowHands.add(new EvaluationResult(HandRank.HIGHCARD, new Card[]{new Card(Rank.ACE, Suit.CLUBS),new Card(Rank.QUEEN, Suit.CLUBS),new Card(Rank.TEN, Suit.CLUBS),new Card(Rank.SEVEN, Suit.CLUBS),new Card(Rank.FOUR, Suit.CLUBS)}));

    }

    @Test
    public void testCompareTo(){
        for(int i = 0; i < highHands.size(); i++){
            for(int j = i; j < lowHands.size(); j++){
                if(i == 0 && j == 0)assertEquals(0, highHands.get(i).compareTo(lowHands.get(j)));
                else assertEquals(-1, highHands.get(i).compareTo(lowHands.get(j)));
            }
        }
    }
}
