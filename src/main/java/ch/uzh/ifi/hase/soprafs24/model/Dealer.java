package ch.uzh.ifi.hase.soprafs24.model;

import java.util.ArrayList;

public class Dealer {
    private Deck deck;

    public Dealer(Deck deck) {
        this.deck = deck;
    }

   public void dealPlayers(ArrayList<Player> players, int currentPlayer) {
        for(int i = 0; i < 2; i++){
            for(int j = currentPlayer; j < currentPlayer + players.size(); j++){
                players.get(j % players.size()).hand[i] = deck.drawCard();
            }
        }
   }

   public void deal(ArrayList<Card> riverCards, int count){
        assert count > 0;
        for(int i = 0; i < count; i++){
            riverCards.add(deck.drawCard());
        }
   }

   public void restore(){
        deck.recycle();
   }
}
