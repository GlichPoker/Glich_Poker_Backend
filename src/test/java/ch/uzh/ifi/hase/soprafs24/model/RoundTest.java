package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.Rank;
import ch.uzh.ifi.hase.soprafs24.constant.Suit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


class RoundTest {

    @Test
    void testRoundComplete_singleWinner() {
        // Arrange
        Player player1 = new Player(1, "hallo", 1000);
        Player player2 = new Player(2, "hallo2", 1000);
        player1.setHand(new Card[] { new Card(Rank.ACE, Suit.CLUBS), new Card(Rank.KING, Suit.HEARTS) });
        player2.setHand(new Card[] { new Card(Rank.QUEEN, Suit.DIAMONDS), new Card(Rank.TEN, Suit.SPADES)});
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(player1, player2));
        Round round = new Round(players, 0, true, null);

        round.communityCards = new ArrayList<>(Arrays.asList(
                new Card(Rank.EIGHT, Suit.HEARTS),
                new Card(Rank.JACK, Suit.CLUBS),
                new Card(Rank.QUEEN, Suit.SPADES),
                new Card(Rank.KING, Suit.DIAMONDS),
                new Card(Rank.ACE, Suit.SPADES)
        ));

        // Act
        List<Player> winners = round.roundComplete();

        // Assert
        assertEquals(1, winners.size());
        assertEquals(player2, winners.get(0));
    }

    @Test
    void testRoundComplete_multipleWinners() {
        // Arrange
        Player player1 = new Player(1, "hallo", 1000);
        Player player2 = new Player(2, "hallo2", 1000);
        player1.setHand(new Card[] { new Card(Rank.ACE, Suit.CLUBS), new Card(Rank.KING, Suit.HEARTS)});
        player2.setHand(new Card[] { new Card(Rank.ACE, Suit.DIAMONDS), new Card(Rank.KING, Suit.SPADES)});
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(player1, player2));
        Round round = new Round(players, 0, true, null);

        round.communityCards = new ArrayList<>(Arrays.asList(
                new Card(Rank.TEN, Suit.HEARTS),
                new Card(Rank.JACK, Suit.CLUBS),
                new Card(Rank.QUEEN, Suit.SPADES),
                new Card(Rank.KING, Suit.DIAMONDS),
                new Card(Rank.ACE, Suit.SPADES)
        ));

        // Act
        List<Player> winners = round.roundComplete();

        // Assert
        assertEquals(2, winners.size());
        assertTrue(winners.contains(player1));
        assertTrue(winners.contains(player2));
    }

    @Test
    void testCalculateWinnings() {
        // Arrange
        Player player1 = new Player(1, "hallo", 1000);
        Player player2 = new Player(2, "hallo2", 1000);
        player1.setRoundBet(200);
        player2.setRoundBet(200);
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(player1, player2));
        Round round = new Round(players, 0, true, null);

        round.potSize = 400;

        // Act
        Map<Player, Double> winnings = round.calculateWinnings(players);

        // Assert
        assertEquals(2, winnings.size());
        assertEquals(200, winnings.get(player1), 0.01);
        assertEquals(200, winnings.get(player2), 0.01);
    }

    @Test
    void testCalculateWinnings_withUnevenPot() {
        // Arrange
        Player player1 = new Player(1, "hallo", 1000);
        Player player2 = new Player(2, "hallo2", 1000);
        player1.setRoundBet(300);
        player2.setRoundBet(100);
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(player1, player2));
        Round round = new Round(players, 0, true, null);

        round.potSize = 400;

        // Act
        Map<Player, Double> winnings = round.calculateWinnings(players);

        // Assert
        assertEquals(2, winnings.size());
        assertEquals(300, winnings.get(player1), 0.01);
        assertEquals(100, winnings.get(player2), 0.01);
    }
}
