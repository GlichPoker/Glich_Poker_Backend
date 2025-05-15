package ch.uzh.ifi.hase.soprafs24.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DealerTest {

    private Deck deck;
    private Dealer dealer;
    private List<Player> players;
    private List<Card> cards;
    
    @BeforeEach
    void setup(){
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
    void testDealPlayers(){
        assertNull(players.get(0).getHand()[0]);

        dealer.dealPlayers(players, 0, 2);
        assertNotNull( players.get(0).getHand()[0]);
        assertNotNull(players.get(1).getHand()[0]);
        assertNotNull(players.get(2).getHand()[0]);
        assertNotNull(players.get(0).getHand()[1]);
        assertNotNull(players.get(1).getHand()[1]);
        assertNotNull(players.get(2).getHand()[1]);
    }

    @Test
    void testDealOne(){
        assertEquals(0, cards.size());
        dealer.deal(cards, 1);
        assertNotNull(cards.get(0));
    }

    @Test
    void testDealThree(){
        assertEquals(0, cards.size());
        dealer.deal(cards, 3);
        assertNotNull(cards.get(1));
        assertNotNull(cards.get(2));
        assertNotNull(cards.get(0));
    }

    @Test
    void testDealZero(){
        assertEquals(0, cards.size());
        dealer.deal(cards, 0);
        assertEquals(0, cards.size());
    }

    @Test
    void testRestore(){
        dealer.deal(cards, 1);
        assertEquals(51, deck.getCards().size());
        dealer.restore();
        assertEquals(52, deck.getCards().size());
    }
}
