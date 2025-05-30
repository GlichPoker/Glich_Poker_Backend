package ch.uzh.ifi.hase.soprafs24.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Round {
    private final List<Player> players;
    protected long potSize;
    protected long roundBet;
    private final Dealer dealer;
    private int playersTurn;
    private final int startPlayer;
    private int betState;
    private final GameSettings gameSettings;
    protected List<Card> communityCards;
    private boolean roundOver;

    private int haveNotRaiseCount;

    public Round(List<Player> players, int startPlayer, boolean isTest, GameSettings gameSettings, int dealCount) {
        this.players = players;
        this.dealer = new Dealer(new Deck());
        this.startPlayer = startPlayer;
        playersTurn = startPlayer;
        betState = 0;
        potSize = 0;
        communityCards = new ArrayList<>();
        roundBet = 0;
        this.gameSettings = gameSettings;
        this.roundOver = false;
        haveNotRaiseCount = 0;
        if(!isTest) {
            dealPlayers(dealCount); // deal with this for tests
        }
        handleBlinds();
        // notify update
    }

    public Round(List<Player> players, int startPlayer, GameSettings gameSettings, int dealCount) {
        this(players, startPlayer, false, gameSettings, dealCount);
    }


    public int getBetState(){
        return betState;
    }
    public boolean isRoundOver() {
        return roundOver;
    }

    public void setIsRoundOver(boolean roundOver) {
        this.roundOver = roundOver;
    }

    public void setHaveNotRaiseCount(int count) {
        haveNotRaiseCount = count;
    }

    public Map<Long, Double> onRoundCompletion(GameSettings settings) {
        List<Player> winners = roundComplete(settings);
        Map<Player, Double> winnings = calculateWinnings(winners);

        updateBalances(winnings);

        this.roundOver = true;

        return winnings.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getUserId(),
                        Map.Entry::getValue));
    }

    public void handleBlinds() {
        // happens only in tests bc i am lazy
        if (gameSettings == null)
            return;
        Player bigBlind = players.get((playersTurn - 1 + players.size()) % players.size());
        Player smallBlind = players.get((playersTurn - 2 + players.size()) % players.size());
        boolean successfulBig = bigBlind.call(gameSettings.bigBlind());
        boolean successfulSmall = smallBlind.call(gameSettings.smallBlind());
        roundBet = successfulBig ? gameSettings.bigBlind() : successfulSmall ? gameSettings.smallBlind() : 0;
        if (successfulBig)
            potSize += gameSettings.bigBlind();
        if (successfulSmall)
            potSize += gameSettings.smallBlind();
    }

    private void resetPlayers() {
        for (Player player : players) {
            player.resetAfterRound();
        }
    }

    private void updateBalances(Map<Player, Double> winnings) {
        for (Map.Entry<Player, Double> entry : winnings.entrySet()) {
            Player player = entry.getKey();
            player.increaseBalance(entry.getValue());
        }
    }

    public List<PlayerModel> getPlayerModelsOfOtherParticipants(long userId) {
        int playerIdx = IntStream.range(0, players.size())
                .filter(i -> players.get(i).getUserId() == userId)
                .findFirst()
                .orElse(-1);
        if (playerIdx == -1)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found");
        List<Player> otherPlayers = new ArrayList<>();
        otherPlayers.addAll(players.subList(playerIdx + 1, players.size()));
        otherPlayers.addAll(players.subList(0, playerIdx));
        ArrayList<PlayerModel> models = new ArrayList<>();
        otherPlayers.forEach(player -> models.add(new PlayerModel(player)));
        return models;
    }

    public Map<Player, Double> calculateWinnings(List<Player> winners) {
        Map<Player, Double> winnings = new HashMap<>();
        long totalWinningBets = winners.stream().mapToLong(Player::getTotalBet).sum();

        if (totalWinningBets == 0) {
            double equalShare = (double) potSize / winners.size();
            for (Player winner : winners) {
                winnings.put(winner, equalShare);
            }
            return winnings;
        }

        for (Player winner : winners) {
            double amount = (long) Math.ceil((double) winner.getTotalBet() / totalWinningBets * potSize);
            winnings.put(winner, amount);
        }
        return winnings;
    }

    public List<Player> roundComplete(GameSettings settings) {
        List<Player> winners = new ArrayList<>();
        EvaluationResult winner = null;
        for (Player player : players) {
            List<Card> cleanedCards = Arrays.stream(player.getHand()).filter(Objects::nonNull).toList();
            List<Card> mergedCards = mergeHands(cleanedCards);

            EvaluationResult res = HandEvaluator.evaluateHand(mergedCards, settings);
            player.setEvaluationResult(res);
            if(!player.isActive()) continue;
            if (winner == null || res.compareTo(winner) < 0) {
                winners.clear();
                winner = res;
                winners.add(player);
            } else if (res.compareTo(winner) == 0) {
                winners.add(player);
            }
        }
        return winners;
    }

    private List<Card> mergeHands(List<Card> hand) {
        ArrayList<Card> handCards = new ArrayList<>();
        for (int i = 0; i < communityCards.size(); i++) {
            if (i < 2) {
                handCards.add(communityCards.get(i));
                handCards.add(hand.get(i));
            } else {
                handCards.add(communityCards.get(i));
            }
        }
        return handCards;
    }

    public void progressRound() {
        haveNotRaiseCount = 0;
        playersTurn = startPlayer;
        while (!players.get(playersTurn).isActive()){
            playersTurn = (playersTurn + 1) % players.size();
        }
        betState++;
        switch (betState) {
            case 1:
                dealFlop();
                break;
            case 2:
                dealTurn();
                break;
            case 3:
                dealRiver();
                break;
            case 4:
                roundOver = true;
                break;
            default:
                break;
        }
        resetPlayers();
        roundBet = 0;
        // notify update client
    }

    private void dealPlayers(int cnt) {
        dealer.dealPlayers(players, playersTurn, cnt);
    }

    private void dealFlop() {
        dealer.deal(communityCards, 3);
    }

    private void dealTurn() {
        dealer.deal(communityCards, 1);
    }

    private void dealRiver() {
        dealer.deal(communityCards, 1);
        dealer.restore();
    }

    public void progressPlayer() {
        do {
            haveNotRaiseCount++;
            playersTurn = (playersTurn + 1) % players.size();
        } while (!players.get(playersTurn).isActive());

        long activePlayers = players.stream().filter(Player::isActive).count();
        if(players.size() < 2 || activePlayers < 2){
            roundOver = true;
        }
        if (shouldProgressRound()) {
            progressRound();
        }
    }

    public Player findPlayerById(long userId) {
        return players.stream()
                .filter(p -> p.getUserId() == userId)
                .findFirst()
                .orElse(null);
    }

    public void handleFold(long userId) {
        for (Player p : players) {
            if (p.getUserId() == userId) {
                if (!p.isActive())
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "User already folded");

                p.fold();
                haveNotRaiseCount--;
                progressPlayer();
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found");
    }

    public void handleCall(long userId, long balance) {
        boolean isRaise = balance > roundBet;
        if (isRaise) {
            haveNotRaiseCount = 0;
        }
        handleCallOrRaise(userId, balance, isRaise);
        progressPlayer();
    }

    public void handleRaise(long userId, long balance) {
        haveNotRaiseCount = 0;
        handleCallOrRaise(userId, balance, true);
        progressPlayer();
    }

    public void handleCheck(long userId) {
        Player player = findPlayerById(userId);
        if (player == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "player not found");
        if (!player.isActive())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "player already folded");
        if(player.getBalance() == 0){
            // player is all in so continue
            progressPlayer();

            return;
        }
        if (player.getRoundBet() < roundBet)
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "cannot check if the player has bet less than the current bet amount");
        progressPlayer();
    }

    private void handleCallOrRaise(long userId, long bet, boolean raise) {
        Player player = findPlayerById(userId);
        if (player == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "player not found");
        if (!player.isActive())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "player already folded");
        if(player.getBalance() == 0){
            // player is all in so continue
            return;
        }
        if(player.getBalance() < bet && player.getBalance() != bet){
            bet = (long) Math.floor(player.getBalance());
        }

        boolean successful = player.call(bet);

        if (successful || player.getBalance() == bet) {
            potSize += bet;
            roundBet = raise ? roundBet + bet : Math.max(roundBet, bet);
        }
    }

    private boolean shouldProgressRound() {
        long activePlayers = players.stream().filter(Player::isActive).count();
        return (haveNotRaiseCount >= activePlayers);
    }

    public GameSettings getGameSettings() {
        return gameSettings;
    }

    public int getPlayersTurn() {
        return playersTurn;
    }

    public int getStartPlayer() {
        return startPlayer;
    }

    public List<Player> getPlayers() {
        return players;
    }
    
    public Card[] updatePlayerHand(long userId, Card card){
        Player player = findPlayerById(userId);
        if (player == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found");
        }
        Card[] hand = player.getHand();
        if(hand[0] == null || hand[1] == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player has no cards");

        }
        int idx = hand[0].equals(card) ? 0 : hand[1].equals(card) ? 1 : -1;
        if(idx == -1){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player does not have this card");
        }
        Card newCard = dealer.randomCard();
        player.setCard(newCard, idx);
        return player.getHand();
    }

    public List<Card> getRemainingCards() {
        return dealer.getRemainingCards();
    }
}
