package ch.uzh.ifi.hase.soprafs24.model;

import java.util.List;

public class Dealer {
    private final Deck deck;

    public Dealer(Deck deck) {
        this.deck = deck;
    }

    public void dealPlayers(List<Player> players, int currentPlayer, int cnt) {
        for (int i = 0; i < cnt; i++) {
            for (int j = currentPlayer; j < currentPlayer + players.size(); j++) {
                players.get(j % players.size()).setHand(deck.drawCard(), i);
            }
        }
    }

    public void deal(List<Card> riverCards, int count) {
        if (count < 1)
            return;
        for (int i = 0; i < count; i++) {
            riverCards.add(deck.drawCard());
        }
    }

    public Card randomCard(){
        return deck.drawCard();
    }

    public void restore() {
        deck.recycle();
    }
}
