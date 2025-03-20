package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.Rank;
import ch.uzh.ifi.hase.soprafs24.constant.Suit;

import java.util.ArrayList;
import java.util.Collections;

public class Deck {
    private ArrayList<Card> cards;

    public Deck(){
        cards = new ArrayList<>();
        recycle();
    }

    public void recycle(){
        cards.clear();
        for(Suit s : Suit.values()){
            for(Rank r : Rank.values()){
                cards.add(new Card(r, s));
            }
        }
        shuffle(cards);
    }

    public void shuffle(ArrayList<Card> cards){
        Collections.shuffle(cards);
    }

    public Card drawCard(){
        return cards.isEmpty() ? null : cards.remove(0);
    }
}
