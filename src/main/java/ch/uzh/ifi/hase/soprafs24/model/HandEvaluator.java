package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.EvaluationRank;
import ch.uzh.ifi.hase.soprafs24.constant.HandRank;
import ch.uzh.ifi.hase.soprafs24.constant.Rank;
import ch.uzh.ifi.hase.soprafs24.constant.Suit;

import java.util.*;
import java.util.stream.Collectors;

public class HandEvaluator {

    private HandEvaluator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static EvaluationResult evaluateHand(List<Card> cards, GameSettings settings) {
         orderCards(cards, settings.descending());
         // suit never matters for high card comparison, so I default to spades
         // technically royal flush is redundant
        int first = 9;

        for (HandRank rank : settings.order()) {
            switch (rank) {
                case ROYALFLUSH:
                    if (isRoyalFlush(cards))
                        return new EvaluationResult(HandRank.ROYALFLUSH, new Card[]{new Card(settings.descending()? Rank.ACE : Rank.TWO, Suit.SPADES)}, EvaluationRank.fromValue(first), settings.descending()); // high card trivially ace
                    break;
                case STRAIGHTFLUSH:
                    if (isStraightFlush(cards))
                        return new EvaluationResult(HandRank.STRAIGHTFLUSH, getHighCardsStraight(cards), EvaluationRank.fromValue(first), settings.descending()); // only high card of straight matters
                    break;
                case FOUROFKIND:
                    if(isFourOfAKind(cards))
                        return new EvaluationResult(HandRank.FOUROFKIND, getHighCardFourKind(cards), EvaluationRank.fromValue(first), settings.descending()); // high card of fours and high card of general hand
                    break;
                case FULLHOUSE:
                    if(isFullHouse(cards))
                        return new EvaluationResult(HandRank.FULLHOUSE, getHighCardsFullHouse(cards), EvaluationRank.fromValue(first), settings.descending()); // high card are trips and doubles
                    break;
                case FLUSH:
                    if(isFlush(cards))
                        return new EvaluationResult(HandRank.FLUSH, getHighCardsFlush(cards), EvaluationRank.fromValue(first), settings.descending()); // high cards are just first 5 flush cards
                    break;
                case STRAIGHT:
                    if(isStraight(cards))
                        return new EvaluationResult(HandRank.STRAIGHT, getHighCardsStraight(cards), EvaluationRank.fromValue(first), settings.descending()); // highest hard of straight
                    break;
                case THREEOFKIND:
                    if(isThreeOfAKind(cards))
                        return new EvaluationResult(HandRank.THREEOFKIND, getHighCardThreeKind(cards), EvaluationRank.fromValue(first), settings.descending()); // value of trips and two high cards must be considered
                    break;
                case TWOPAIR:
                    if(isTwoPair(cards))
                        return new EvaluationResult(HandRank.TWOPAIR, getHighCardsTwoPair(cards), EvaluationRank.fromValue(first), settings.descending()); // value of both pairs and one additional high card must be considered
                    break;
                case ONEPAIR:
                    if(isPair(cards))
                        return new EvaluationResult(HandRank.ONEPAIR, getHighCardTwoKind(cards), EvaluationRank.fromValue(first), settings.descending()); //value of pair and three high cards must be considered
                    break;
                case HIGHCARD:
                    return new EvaluationResult(HandRank.HIGHCARD, getHighCards(cards), EvaluationRank.fromValue(first), settings.descending()); // 5 highest cards need to be considered
            }
            first--;
        }
        return null; // Can never happen but java stupid
    }

    private static boolean isRoyalFlush(List<Card> cards) {
        List<Rank> royalFlushRanks = Arrays.asList(Rank.TEN, Rank.JACK, Rank.QUEEN, Rank.KING, Rank.ACE);
        long count = cards.stream()
                .filter(card -> royalFlushRanks.contains(card.rank()))
                .map(Card::suit)
                .distinct()
                .count();
        return count == 1 && cards.stream().filter(card -> royalFlushRanks.contains(card.rank())).count() == 5;
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
        return cards.stream().filter(x -> x.suit() == suit).limit(5).toArray(Card[]::new);
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
        Optional<Card> highCardRankCard = cards.stream().filter(x -> x.rank() != ranks[0] && x.rank() != ranks[1]).findFirst();
        return highCardRankCard.map(card -> new Card[]{new Card(ranks[0], Suit.SPADES), new Card(ranks[1], Suit.SPADES), new Card(card.rank(), Suit.SPADES)}).orElseGet(() -> new Card[]{new Card(ranks[0], Suit.SPADES), new Card(ranks[1], Suit.SPADES)});
    }

    private static Card[] getHighCardTwoKind(List<Card> cards) {
        Rank pair = findHighPairOfSize(cards, 2);
        Rank[] high = getRemainingHighCards(cards, 3, pair);
        return new Card[]{new Card(pair, Suit.SPADES), new Card(high[0], Suit.SPADES), new Card(high[1], Suit.SPADES), new Card(high[2], Suit.SPADES)};
    }

    private static Card[] getHighCards(List<Card> cards) {
        return cards.stream().limit(5).toArray(Card[]::new);
    }

    private static Rank findHighPairOfSize(List<Card> cards, int size) {
        int c = 1;
        Rank high = null;
        for(int i = 1; i < cards.size(); i++) {
            if(cards.get(i).rank() == cards.get(i - 1).rank()) c++;
            else c = 1;
            if(c == size) {
                high = cards.get(i).rank();
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
            if(cards.get(i).rank() == cards.get(i - 1).rank()) c++;
            else c = 1;
            if(c == 2 && pairR == null) {
                pairR = cards.get(i).rank();
            }
            else if(c == 3 && tripsR == null && (pairR == null || pairR != cards.get(i).rank())) {
                tripsR = cards.get(i).rank();
            }
            else if(c == 3 && pairR == cards.get(i).rank() && tripsR == null){
                tripsR = cards.get(i).rank();
                pairR = null;
            }
        }
        return new Rank[]{tripsR, pairR};
    }

    private static Suit getFlush(List<Card> cards) {
        Suit flush = null;
        Map<Suit, List<Card>> distribution = cards.stream().collect(Collectors.groupingBy(Card::suit));
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
        List<Rank> distinctRanks = cards.stream().map(Card::rank).distinct().toList();
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
            if(cards.get(i).rank() == cards.get(i - 1).rank()) c++;
            else c = 1;
            if(c == 2) {
                ranks[currentIdx] = cards.get(i).rank();
                currentIdx++;
            }
            if(currentIdx > 1) break;
        }
        return ranks;
    }

    private static Rank[] getRemainingHighCards(List<Card> cards, int take, Rank pair) {
        return cards.stream().map(Card::rank).filter(x -> x != pair).limit(take).toArray(Rank[]::new);
    }

    private static void orderCards(List<Card> cards, boolean descending) {
        cards.sort(descending ? Comparator.comparing(Card::rank).reversed(): Comparator.comparing(Card::rank));
    }
}
