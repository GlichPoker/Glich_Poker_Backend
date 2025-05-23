package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.EvaluationRank;
import ch.uzh.ifi.hase.soprafs24.constant.HandRank;
import ch.uzh.ifi.hase.soprafs24.constant.Rank;
import ch.uzh.ifi.hase.soprafs24.constant.Suit;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                        return new EvaluationResult(HandRank.ROYALFLUSH, getHighCardsRoyalStraight(cards), EvaluationRank.fromValue(first), settings.descending(), getActualRoyalStraight(cards, settings)); // high card trivially ace
                    break;
                case STRAIGHTFLUSH:
                    if (isStraightFlush(cards))
                        return new EvaluationResult(HandRank.STRAIGHTFLUSH, getHighCardsRoyalStraight(cards), EvaluationRank.fromValue(first), settings.descending(), getActualRoyalStraight(cards, settings)); // only high card of straight matters
                    break;
                case FOUROFKIND:
                    if(isFourOfAKind(cards))
                        return new EvaluationResult(HandRank.FOUROFKIND, getHighCardFourKind(cards), EvaluationRank.fromValue(first), settings.descending(), getActualHandFourKind(cards)); // high card of fours and high card of general hand
                    break;
                case FULLHOUSE:
                    if(isFullHouse(cards))
                        return new EvaluationResult(HandRank.FULLHOUSE, getHighCardsFullHouse(cards), EvaluationRank.fromValue(first), settings.descending(), getActualFullHouse(cards)); // high card are trips and doubles
                    break;
                case FLUSH:
                    if(isFlush(cards))
                        return new EvaluationResult(HandRank.FLUSH, getHighCardsFlush(cards), EvaluationRank.fromValue(first), settings.descending(), getHighCardsFlush(cards)); // high cards are just first 5 flush cards
                    break;
                case STRAIGHT:
                    if(isStraight(cards))
                        return new EvaluationResult(HandRank.STRAIGHT, getHighCardsStraight(cards), EvaluationRank.fromValue(first), settings.descending(), getActualStraight(cards, settings)); // highest hard of straight
                    break;
                case THREEOFKIND:
                    if(isThreeOfAKind(cards))
                        return new EvaluationResult(HandRank.THREEOFKIND, getHighCardThreeKind(cards), EvaluationRank.fromValue(first), settings.descending(), getActualHandThreeOfAKind(cards)); // value of trips and two high cards must be considered
                    break;
                case TWOPAIR:
                    if(isTwoPair(cards))
                        return new EvaluationResult(HandRank.TWOPAIR, getHighCardsTwoPair(cards), EvaluationRank.fromValue(first), settings.descending(), getActualHandTwoPair(cards)); // value of both pairs and one additional high card must be considered
                    break;
                case ONEPAIR:
                    if(isPair(cards))
                        return new EvaluationResult(HandRank.ONEPAIR, getHighCardTwoKind(cards), EvaluationRank.fromValue(first), settings.descending(), getActualHandPair(cards)); //value of pair and three high cards must be considered
                    break;
                case HIGHCARD:
                    return new EvaluationResult(HandRank.HIGHCARD, getHighCards(cards), EvaluationRank.fromValue(first), settings.descending(), getHighCards(cards)); // 5 highest cards need to be considered
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
        Card[] ranks = getFullHouseRanks(cards);
        return ranks[0] != null && ranks[1] != null;
    }

    private static boolean isFlush(List<Card> cards) {
        return getFlush(cards) != null;
    }

    private static boolean isStraight(List<Card> cards) {
        Card card = getStraightRank(cards);
        return card.rank() != null;
    }

    private static boolean isThreeOfAKind(List<Card> cards) {
        return countPairOfSize(cards, 3);
    }

    private static boolean isTwoPair(List<Card> cards) {
        Card[] ranks = findTwoPairRanks(cards);
        return ranks[0]!= null && ranks[1] != null;
    }

    private static boolean isPair(List<Card> cards) {
        return countPairOfSize(cards, 2);
    }

    private static boolean countPairOfSize(List<Card> cards, int count) {
        Card high = findHighPairOfSize(cards, count);
        return high != null;
    }

    private static Card[] getHighCardFourKind(List<Card> cards) {
        Card pair = findHighPairOfSize(cards, 4);
        Card[] high = getRemainingHighCards(cards, 1, pair.rank());
        return new Card[]{pair, high[0]};
    }

    private static Card[] getActualHandFourKind(List<Card> cards) {
        return getActualPairs(cards, 4, 1);
    }

    private static Card[] getActualHandThreeOfAKind(List<Card> cards) {
        return getActualPairs(cards, 3, 2);
    }

    private static Card[] getActualHandPair(List<Card> cards) {
        return getActualPairs(cards, 2, 3);
    }

    private static Card[] getHighCardsFullHouse(List<Card> cards) {
        return getFullHouseRanks(cards);
    }

    private static Card[] getActualFullHouse(List<Card> cards) {
        Card[] ranks = getFullHouseRanks(cards);
        Card[] trips = cards.stream().filter(x -> x.rank() == ranks[0].rank()).toArray(Card[]::new);
        Card[] doubles = cards.stream().filter(x -> x.rank() == ranks[1].rank()).toArray(Card[]::new);
        Card[] newArr = new Card[2];

        if (doubles.length > 2) {
            newArr[0] = doubles[0];
            newArr[1] = doubles[1];
        }
        return mergeArrays(trips, newArr);
    }
    private static Card[] getHighCardsFlush(List<Card> cards){
        Suit suit = getFlush(cards);
        return cards.stream().filter(x -> x.suit() == suit).limit(5).toArray(Card[]::new);
    }

    private static Card[] getHighCardsStraight(List<Card> cards) {
        Card card = getStraightRank(cards);
        return new Card[]{card};
    }

    private static Card[] getHighCardsRoyalStraight(List<Card> cards) {
        Card card = getRoyalStraightRank(cards);
        return new Card[]{card};
    }

    private static Card[] getActualStraight(List<Card> cards, GameSettings gameSettings) {
        Card card = getStraightRank(cards);
        Set<Rank> seenRanks = new HashSet<>();
        return cards.stream()
                .filter(x -> x.rank().value <= card.rank().value)
                .filter(x -> seenRanks.add(x.rank())) // keeps only first card of each rank
                .limit(5)
                .toArray(Card[]::new);
    }

    private static Card[] getActualRoyalStraight(List<Card> cards, GameSettings gameSettings) {
        Card card = getRoyalStraightRank(cards);
        Card[] res = new Card[5];
        res[0] = card;
        int adder = gameSettings.descending() ? -1 : 1;
        int c = 1;
        for(int i = card.rank().value + adder; i >= card.rank().value + (adder * 4); i+=adder) {
            res[c] = new Card(Rank.fromValue(i), card.suit());
        }
        return res;
    }

    private static Card[] getHighCardThreeKind(List<Card> cards) {
        Card pair = findHighPairOfSize(cards, 3);
        Card[] high = getRemainingHighCards(cards, 2, pair.rank());
        return new Card[]{pair, high[0], high[1]};
    }

    private static Card[] getHighCardsTwoPair(List<Card> cards) {
        Card[] ranks = findTwoPairRanks(cards);
        Optional<Card> highCardRankCard = cards.stream().filter(x -> x.rank() != ranks[0].rank() && x.rank() != ranks[1].rank()).findFirst();
        return highCardRankCard.map(card -> new Card[]{ranks[0], ranks[1], card})
                .orElseGet(() -> ranks);
    }

    private static Card[] getActualHandTwoPair(List<Card> cards) {
        Card[] ranks = findTwoPairRanks(cards);

        Card[] actualRanks = cards.stream().filter(x -> x.rank() == ranks[1].rank() || x.rank() == ranks[0].rank()).toArray(Card[]::new);
        Optional<Card> highCardRankCard = cards.stream().filter(x -> x.rank() != ranks[0].rank() && x.rank() != ranks[1].rank()).findFirst();
        return highCardRankCard.map(card -> mergeArrays(actualRanks, new Card[]{card})).orElse(actualRanks);
    }

    private static Card[] getHighCardTwoKind(List<Card> cards) {
        Card pair = findHighPairOfSize(cards, 2);
        Card[] high = getRemainingHighCards(cards, 3, pair.rank());
        return new Card[]{pair, high[0], high[1], high[2]};
    }

    private static Card[] getHighCards(List<Card> cards) {
        return cards.stream().limit(5).toArray(Card[]::new);
    }

    private static Card findHighPairOfSize(List<Card> cards, int size) {
        int c = 1;
        Card card = null;
        for(int i = 1; i < cards.size(); i++) {
            if(cards.get(i).rank() == cards.get(i - 1).rank()) c++;
            else c = 1;
            if(c == size) {
                card = cards.get(i);
                break;
            }
        }
        return card;
    }

    private static Card[] getFullHouseRanks(List<Card> cards) {
        int c = 1;
        Card pairR = null;
        Card tripsR = null;
        for(int i = 1; i < cards.size(); i++) {
            if(cards.get(i).rank() == cards.get(i - 1).rank()) c++;
            else c = 1;
            if(c == 2 && pairR == null) {
                pairR = cards.get(i);
            }
            else if(c == 3 && tripsR == null && (pairR == null || pairR.rank() != cards.get(i).rank())) {
                tripsR = cards.get(i);
            }
            else if(c == 3 && pairR.rank() == cards.get(i).rank() && tripsR == null){
                tripsR = cards.get(i);
                pairR = null;
            }
        }
        return new Card[]{tripsR, pairR};
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
    private static Card getRoyalStraightRank(List<Card> cards) {
        Rank rank = null;
        Suit suit = null;
        int consecutive = 1;
        Suit flush = getFlush(cards);
        List<Rank> distinctRanks = cards.stream().filter(x -> x.suit() == flush).map(Card::rank).distinct().toList();
        for(int i = 1; i < distinctRanks.size(); i++) {
            consecutive = distinctRanks.get(i).value + 1 == distinctRanks.get(i - 1).value ?
                    consecutive + 1 : 1;
            if(consecutive == 5) {
                rank = distinctRanks.get(i - 4);
                suit = getFlush(cards);
                break;
            }
        }
        return new Card(rank, suit);
    }

    private static Card getStraightRank(List<Card> cards) {
        Rank rank = null;
        Suit suit = null;
        int consecutive = 1;
        List<Rank> distinctRanks = cards.stream().map(Card::rank).distinct().toList();
        for(int i = 1; i < distinctRanks.size(); i++) {
            consecutive = distinctRanks.get(i).value + 1 == distinctRanks.get(i - 1).value ?
                    consecutive + 1 : 1;
            if(consecutive == 5) {
                rank = distinctRanks.get(i - 4);
                Rank finalRank = rank;
                suit = cards.stream()
                        .filter(x -> x.rank().value == finalRank.value)
                        .map(Card::suit)
                        .findFirst()
                        .orElse(Suit.SPADES);
                break;
            }
        }
        return new Card(rank, suit);
    }

    private static Card[] findTwoPairRanks(List<Card> cards) {
        int c = 1;
        int currentIdx = 0;
        Card[] ranks = new Card[2];
        for(int i = 1; i < cards.size(); i++) {
            if(cards.get(i).rank() == cards.get(i - 1).rank()) c++;
            else c = 1;
            if(c == 2) {
                ranks[currentIdx] = cards.get(i);
                currentIdx++;
            }
            if(currentIdx > 1) break;
        }
        return ranks;
    }

    private static Card[] getRemainingHighCards(List<Card> cards, int take, Rank pair) {
        return cards.stream().filter(x -> x.rank() != pair).limit(take).toArray(Card[]::new);
    }

    private static void orderCards(List<Card> cards, boolean descending) {
        cards.sort(descending ? Comparator.comparing(Card::rank).reversed(): Comparator.comparing(Card::rank));
    }

    private static Card[] mergeArrays(Card[] first, Card[] second) {
        return Stream.concat(Arrays.stream(first), Arrays.stream(second)).toArray(Card[]::new);
    }

    private static Card[] getActualPairs(List<Card> cards, int size, int take) {
        Card pair = findHighPairOfSize(cards, size);
        Card[] pairs = cards.stream().filter(x -> x.rank() == pair.rank()).toArray(Card[]::new);
        Card[] high = getRemainingHighCards(cards, take, pair.rank());
        return mergeArrays(pairs, high);
    }
}
