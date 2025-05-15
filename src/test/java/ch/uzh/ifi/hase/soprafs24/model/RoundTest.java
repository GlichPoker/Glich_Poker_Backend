package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.HandRank;
import ch.uzh.ifi.hase.soprafs24.constant.Rank;
import ch.uzh.ifi.hase.soprafs24.constant.Suit;
import ch.uzh.ifi.hase.soprafs24.constant.WeatherType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;


class RoundTest {

    public static GameSettings gameSettings;
    @BeforeAll
    static void setup() {
        List<HandRank> order = new ArrayList<>(Arrays.stream(HandRank.values()).sorted(Comparator.reverseOrder()).toList());
        gameSettings = new GameSettings(1000,10,20, order, true, WeatherType.DEFAULT, "");
    }
    @Test
    void testRoundComplete_singleWinner() {
        // Arrange
        Player player1 = new Player(1, "hallo", 1000);
        Player player2 = new Player(2, "hallo2", 1000);
        player1.setHand(new Card[] { new Card(Rank.ACE, Suit.CLUBS), new Card(Rank.KING, Suit.HEARTS) });
        player2.setHand(new Card[] { new Card(Rank.QUEEN, Suit.DIAMONDS), new Card(Rank.TEN, Suit.SPADES)});
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(player1, player2));
        Round round = new Round(players, 0, true, null, 2);

        round.communityCards = new ArrayList<>(Arrays.asList(
                new Card(Rank.EIGHT, Suit.HEARTS),
                new Card(Rank.JACK, Suit.CLUBS),
                new Card(Rank.QUEEN, Suit.SPADES),
                new Card(Rank.KING, Suit.DIAMONDS),
                new Card(Rank.ACE, Suit.SPADES)
        ));

        // Act
        List<Player> winners = round.roundComplete(gameSettings);

        // Assert
        assertEquals(1, winners.size());
        assertEquals(player2.getUserId(), winners.get(0).getUserId());
    }

    @Test
    void testRoundComplete_multipleWinners() {
        // Arrange
        Player player1 = new Player(1, "hallo", 1000);
        Player player2 = new Player(2, "hallo2", 1000);
        player1.setHand(new Card[] { new Card(Rank.ACE, Suit.CLUBS), new Card(Rank.KING, Suit.HEARTS)});
        player2.setHand(new Card[] { new Card(Rank.ACE, Suit.DIAMONDS), new Card(Rank.KING, Suit.SPADES)});
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(player1, player2));
        Round round = new Round(players, 0, true, null, 2);

        round.communityCards = new ArrayList<>(Arrays.asList(
                new Card(Rank.TEN, Suit.HEARTS),
                new Card(Rank.JACK, Suit.CLUBS),
                new Card(Rank.QUEEN, Suit.SPADES),
                new Card(Rank.KING, Suit.DIAMONDS),
                new Card(Rank.ACE, Suit.SPADES)
        ));

        // Act
        List<Player> winners = round.roundComplete(gameSettings);

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
        player1.setTotalBet(200);
        player2.setTotalBet(200);
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(player1, player2));
        Round round = new Round(players, 0, true, null, 2);

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
        player1.setTotalBet(300);
        player2.setTotalBet(100);
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(player1, player2));
        Round round = new Round(players, 0, true, null, 2);

        round.potSize = 400;

        // Act
        Map<Player, Double> winnings = round.calculateWinnings(players);

        // Assert
        assertEquals(2, winnings.size());
        assertEquals(300, winnings.get(player1), 0.01);
        assertEquals(100, winnings.get(player2), 0.01);
    }

    @Test
    void testOnRoundCompletion(){
        Player player1 = new Player(1, "hallo", 1000);
        Player player2 = new Player(2, "hallo2", 1000);
        player1.setTotalBet(300);
        player2.setTotalBet(100);
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(player1, player2));
        Round round = new Round(players, 0, true, null, 2);
        assertEquals(2, round.onRoundCompletion(gameSettings).size());
    }

    @Test
    void testProgressRound() {
        // Arrange
        Player player1 = new Player(1, "hallo", 1000);
        Player player2 = new Player(2, "hallo2", 1000);
        player1.setRoundBet(200);
        player2.setRoundBet(200);
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(player1, player2));
        Round round = new Round(players, 0, true, null, 2);

        round.potSize = 400;

        // Act
        round.progressRound();

        // Assert
        assertEquals(0, player1.getRoundBet());
        assertEquals(0, player2.getRoundBet());

        player1.setRoundBet(200);
        player2.setRoundBet(200);

        // Act
        round.progressRound();

        // Assert
        assertEquals(0, player1.getRoundBet());
        assertEquals(0, player2.getRoundBet());
        player1.setRoundBet(200);
        player2.setRoundBet(200);

        // Act
        round.progressRound();

        // Assert
        assertEquals(0, player1.getRoundBet());
        assertEquals(0, player2.getRoundBet());
        player1.setRoundBet(200);
        player2.setRoundBet(200);

        // Act
        round.progressRound();

        // Assert
        assertEquals(0, player1.getRoundBet());
        assertEquals(0, player2.getRoundBet());
        assertTrue(round.isRoundOver());
    }

    @Test
    void testCalculateWinningsZeroBet() {
        // Arrange
        Player player1 = new Player(1, "hallo", 1000);
        Player player2 = new Player(2, "hallo2", 1000);
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(player1, player2));
        Round round = new Round(players, 0, true, null, 2);

        round.potSize = 0;

        // Act
        Map<Player, Double> winnings = round.calculateWinnings(players);

        // Assert
        assertEquals(2, winnings.size());
        assertEquals(0, winnings.get(player1), 0.01);
        assertEquals(0, winnings.get(player2), 0.01);
    }

    @Test
    void testProgressPlayer() {
        // Arrange
        Player player1 = new Player(1, "hallo", 1000);
        Player player2 = new Player(2, "hallo2", 1000);
        player1.setTotalBet(200);
        player2.setTotalBet(200);
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(player1, player2));
        Round round = new Round(players, 0, true, null, 2);
        round.setHaveNotRaiseCount(1);
        round.setFirstActionOccurred(true);
        round.setHasProgressedOnce(true);
        int oldbetState = round.getBetState();
        round.potSize = 400;

        // Act
        round.progressPlayer();
        // Assert
        assertEquals(oldbetState + 1, round.getBetState());
    }
}
