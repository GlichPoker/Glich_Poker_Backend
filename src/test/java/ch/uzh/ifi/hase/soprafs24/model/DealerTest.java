package ch.uzh.ifi.hase.soprafs24.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DealerTest {

    private Deck deck;
    private Dealer dealer;
    private List<Player> players;
    private List<Card> cards;
    @BeforeEach
    public void setup(){
        deck = new Deck();
        players = new ArrayList<Player>() {{
            add(new Player(1, "a", 100));
            add(new Player(2, "b", 100));
            add(new Player(3, "c", 100));
        }};
        dealer = new Dealer(deck);
        cards = new ArrayList<>();
    }

    @Test
    public void testDealPlayers(){
        assertNull(players.get(0).getHand()[0]);

        dealer.dealPlayers(players, 0);
        assertNotNull( players.get(0).getHand()[0]);
        assertNotNull(players.get(1).getHand()[0]);
        assertNotNull(players.get(2).getHand()[0]);
        assertNotNull(players.get(0).getHand()[1]);
        assertNotNull(players.get(1).getHand()[1]);
        assertNotNull(players.get(2).getHand()[1]);
    }

    @Test
    public void testDealOne(){
        assertEquals(cards.size(), 0);
        dealer.deal(cards, 1);
        assertNotNull(cards.get(0));
    }

    @Test
    public void testDealThree(){
        assertEquals(cards.size(), 0);
        dealer.deal(cards, 3);
        assertNotNull(cards.get(1));
        assertNotNull(cards.get(2));
        assertNotNull(cards.get(0));
    }

    @Test
    public void testDealZero(){
        assertEquals(cards.size(), 0);
        dealer.deal(cards, 0);
        assertEquals(cards.size(), 0);
    }

    @Test
    public void testRestore(){
        dealer.deal(cards, 1);
        assertEquals(deck.getCards().size(), 51);
        dealer.restore();
        assertEquals(deck.getCards().size(), 52);
    }
}
