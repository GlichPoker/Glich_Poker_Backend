package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.HandRank;
import ch.uzh.ifi.hase.soprafs24.constant.Rank;
import ch.uzh.ifi.hase.soprafs24.constant.Suit;

import java.util.*;
import java.util.stream.Collectors;

public class HandEvaluator {

    private HandEvaluator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static EvaluationResult evaluateHand(List<Card> cards) {
         orderCards(cards);
         // suit never matters for high card comparison so I default to spades
         if(isRoyalFlush(cards)) return new EvaluationResult(HandRank.ROYALFLUSH, new Card[] {new Card(Rank.ACE, Suit.SPADES)}); // high card trivially ace
         if(isStraightFlush(cards)) return new EvaluationResult(HandRank.STRAIGHTFLUSH, getHighCardsStraight(cards)); // only high card of straight matters
         if(isFourOfAKind(cards)) return new EvaluationResult(HandRank.FOUROFKIND, getHighCardFourKind(cards)); // high card of fours and high card of general hand
         if(isFullHouse(cards)) return new EvaluationResult(HandRank.FULLHOUSE, getHighCardsFullHouse(cards)); // high card are trips and doubles
         if(isFlush(cards)) return new EvaluationResult(HandRank.FLUSH, getHighCardsFlush(cards)); // high cards are just first 5 flush cards
         if(isStraight(cards)) return new EvaluationResult(HandRank.STRAIGHT, getHighCardsStraight(cards)); // highest hard of straight
         if(isThreeOfAKind(cards)) return new EvaluationResult(HandRank.THREEOFKIND, getHighCardThreeKind(cards)); // value of trips and two high cards must be considered
         if(isTwoPair(cards)) return new EvaluationResult(HandRank.TWOPAIR, getHighCardsTwoPair(cards)); // value of both pairs and one additional high card must be considered
         if(isPair(cards)) return new EvaluationResult(HandRank.ONEPAIR, getHighCardTwoKind(cards)); //value of pair and three high cards must be considered
         return new EvaluationResult(HandRank.HIGHCARD, getHighCards(cards)); // 5 highest cards need to be considered
    }

    private static boolean isRoyalFlush(List<Card> cards) {
        List<Rank> royalFlushRanks = Arrays.asList(Rank.TEN, Rank.JACK, Rank.QUEEN, Rank.KING, Rank.ACE);
        long count = cards.stream()
                .filter(card -> royalFlushRanks.contains(card.getRank()))
                .map(Card::getSuit)
                .distinct()
                .count();
        return count == 1 && cards.stream().filter(card -> royalFlushRanks.contains(card.getRank())).count() == 5;
    }

    private static boolean isStraightFlush(List<Card> cards) {
        return isStraight(cards) && isFlush(cards);
    }

    private static boolean isFourOfAKind(List<Card> cards) {
        return countPairOfSize(cards, 4);
    }

    private static boolean isFullHouse(List<Card> cards) {
        Rank[] ranks = getFullHouseRanks(cards);
        return ranks[0] != null && ranks[1] != null;
    }

    private static boolean isFlush(List<Card> cards) {
        return getFlush(cards) != null;
    }

    private static boolean isStraight(List<Card> cards) {
        Rank rank = getStraightRank(cards);
        return rank != null;
    }

    private static boolean isThreeOfAKind(List<Card> cards) {
        return countPairOfSize(cards, 3);
    }

    private static boolean isTwoPair(List<Card> cards) {
        Rank[] ranks = findTwoPairRanks(cards);
        return ranks[0] != null && ranks[1] != null;
    }

    private static boolean isPair(List<Card> cards) {
        return countPairOfSize(cards, 2);
    }

    private static boolean countPairOfSize(List<Card> cards, int count) {
        Rank high = findHighPairOfSize(cards, count);
        return high != null;
    }

    private static Card[] getHighCardFourKind(List<Card> cards) {
        Rank pair = findHighPairOfSize(cards, 4);
        Rank[] high = getRemainingHighCards(cards, 1, pair);
        return new Card[]{new Card(pair, Suit.SPADES), new Card(high[0], Suit.SPADES)};
    }

    private static Card[] getHighCardsFullHouse(List<Card> cards) {
        Rank[] ranks = getFullHouseRanks(cards);
        return new Card[]{new Card(ranks[0], Suit.SPADES), new Card(ranks[1], Suit.SPADES)};
    }

    private static Card[] getHighCardsFlush(List<Card> cards){
        Suit suit = getFlush(cards);
        return cards.stream().filter(x -> x.getSuit() == suit).limit(5).toArray(Card[]::new);
    }

    private static Card[] getHighCardsStraight(List<Card> cards) {
        Rank rank = getStraightRank(cards);
        return new Card[]{new Card(rank, Suit.SPADES)};
    }

    private static Card[] getHighCardThreeKind(List<Card> cards) {
        Rank pair = findHighPairOfSize(cards, 3);
        Rank[] high = getRemainingHighCards(cards, 2, pair);
        return new Card[]{new Card(pair, Suit.SPADES), new Card(high[0], Suit.SPADES), new Card(high[1], Suit.SPADES)};
    }

    private static Card[] getHighCardsTwoPair(List<Card> cards) {
        Rank[] ranks = findTwoPairRanks(cards);
        Rank highCardRank = cards.stream().filter(x -> x.getRank() != ranks[0] && x.getRank() != ranks[1]).findFirst().get().getRank();
        return new Card[]{new Card(ranks[0], Suit.SPADES), new Card(ranks[1], Suit.SPADES), new Card(highCardRank, Suit.SPADES)};
    }

    private static Card[] getHighCardTwoKind(List<Card> cards) {
        Rank pair = findHighPairOfSize(cards, 2);
        Rank[] high = getRemainingHighCards(cards, 3, pair);
        return new Card[]{new Card(pair, Suit.SPADES), new Card(high[0], Suit.SPADES), new Card(high[1], Suit.SPADES), new Card(high[2], Suit.SPADES)};
    }

    private static Card[] getHighCards(List<Card> cards) {
        return cards.stream().limit(5).toArray(Card[]::new);
    }

    private static Rank findHighPairOfSize(List<Card> cards, int count) {
        int c = 1;
        Rank high = null;
        for(int i = 1; i < cards.size(); i++) {
            if(cards.get(i).getRank() == cards.get(i - 1).getRank()) c++;
            else c = 1;
            if(c == count) {
                high = cards.get(i - (count - 1)).getRank();
                break;
            }
        }
        return high;
    }

    private static Rank[] getFullHouseRanks(List<Card> cards) {
        int c = 1;
        Rank pairR = null;
        Rank tripsR = null;
        for(int i = 1; i < cards.size(); i++) {
            if(cards.get(i).getRank() == cards.get(i - 1).getRank()) c++;
            else c = 1;
            if(c == 2 && pairR == null) {
                pairR = cards.get(i).getRank();
            }
            else if(c == 3 && tripsR == null && (pairR == null || pairR != cards.get(i).getRank())) {
                tripsR = cards.get(i).getRank();
            }
            else if(c == 3 && pairR == cards.get(i).getRank() && tripsR == null){
                tripsR = cards.get(i).getRank();
                pairR = null;
            }
        }
        return new Rank[]{tripsR, pairR};
    }

    private static Suit getFlush(List<Card> cards) {
        Suit flush = null;
        Map<Suit, List<Card>> distribution = cards.stream().collect(Collectors.groupingBy(Card::getSuit));
        for(Map.Entry<Suit, List<Card>> entry : distribution.entrySet()) {
            if(entry.getValue().size() > 4) {
                flush = entry.getKey();
                break;
            }
        }
        return flush;
    }

    private static Rank getStraightRank(List<Card> cards) {
        Rank rank = null;
        int consecutive = 1;
        List<Rank> distinctRanks = cards.stream().map(Card::getRank).distinct().toList();
        for(int i = 1; i < distinctRanks.size(); i++) {
            consecutive = distinctRanks.get(i).value + 1 == distinctRanks.get(i - 1).value ?
                    consecutive + 1 : 1;
            if(consecutive == 5) {
                rank = distinctRanks.get(i - 4);
                break;
            }
        }
        return rank;
    }

    private static Rank[] findTwoPairRanks(List<Card> cards) {
        int c = 1;
        int currentIdx = 0;
        Rank[] ranks = new Rank[2];
        for(int i = 1; i < cards.size(); i++) {
            if(cards.get(i).getRank() == cards.get(i - 1).getRank()) c++;
            else c = 1;
            if(c == 2) {
                ranks[currentIdx] = cards.get(i).getRank();
                currentIdx++;
            }
            if(currentIdx > 1) break;
        }
        return ranks;
    }

    private static Rank[] getRemainingHighCards(List<Card> cards, int take, Rank pair) {
        return cards.stream().map(Card::getRank).filter(x -> x != pair).limit(take).toArray(Rank[]::new);
    }

    private static void orderCards(List<Card> cards) {
        cards.sort(Comparator.comparing(Card::getRank).reversed());
    }
}
