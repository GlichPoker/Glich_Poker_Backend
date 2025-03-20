package ch.uzh.ifi.hase.soprafs24.model;

import java.util.*;

public class Round {
    public ArrayList<Player> players;
    public long potSize;
    public long roundBet;
    public Dealer dealer;
    public int playersTurn;
    public int startPlayer;
    public int betState;
    public int haveNotRaised;
    public ArrayList<Card> communityCards;

    public Round(ArrayList<Player> players, Dealer dealer, int startPlayer) {
        this.players = players;
        this.dealer = dealer;
        this.startPlayer = startPlayer;
        playersTurn = startPlayer;
        betState = 0;
        potSize = 0;
        haveNotRaised = 0;
        communityCards = new ArrayList<>();
        roundBet = 0;
        dealPlayers();
        // notify update
    }

    public void onRoundCompletion(){
        List<Player> winners = roundComplete();
        Map<Player, Double> winnings = calculateWinnings(winners);
        players = updateBalances(winnings);
        // notify client update
    }

    private ArrayList<Player> updateBalances(Map<Player, Double> winnings) {
        for(Map.Entry<Player, Double> entry : winnings.entrySet()) {
            Player player = entry.getKey();
            player.balance += entry.getValue();
        }
        return new ArrayList<>(winnings.keySet().stream().toList());
    }

    public Map<Player, Double> calculateWinnings(List<Player> winners){
        Map<Player, Double> winnings = new Hashtable<>();
        for(Player winner : winners){
            double amount = (double)winner.roundBet / this.potSize * potSize;
            winnings.put(winner, amount);
        }
        return winnings;
    }

    public List<Player> roundComplete(){
    List<Player> winners = new ArrayList<>();
    EvaluationResult winner = null;
    for (Player player : players) {
        ArrayList<Card> mergedCards = mergeHands(player.hand);

        EvaluationResult res = HandEvaluator.evaluateHand(mergedCards);
        if(winner == null || res.compareTo(winner) < 0){
            winners.clear();
            winner = res;
            winners.add(player);
        }
        else if(res.compareTo(winner) == 0){
            winners.add(player);
        }
    }
    return winners;
    }

    private ArrayList<Card> mergeHands(Card[] hand){
        ArrayList<Card> handCards = new ArrayList<>();
        for(int i = 0; i < communityCards.size(); i++){
            if(i < 2){
                handCards.add(communityCards.get(i));
                handCards.add(hand[i]);
            }
            else{
                handCards.add(communityCards.get(i));
            }
        }
        return handCards;
    }

    public void progressRound(){
        playersTurn = startPlayer;
        betState++;
        roundBet = 0;
        switch (betState){
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
                onRoundCompletion();
                break;
            default:
                break;
        }
        // notify update client
    }

    public void dealPlayers(){
        dealer.dealPlayers(players, playersTurn);
    }

    public void dealFlop(){
        dealer.deal(communityCards, 3);
    }

    public void dealTurn(){
        dealer.deal(communityCards, 1);
    }

    public void dealRiver(){
        dealer.deal(communityCards, 1);
        dealer.restore();
    }

    public void progressPlayer(){
        do{
        playersTurn = (playersTurn + 1) % players.size();
        haveNotRaised++;}
        while(!players.get(playersTurn).isActive);
        // notify update
        if(haveNotRaised == players.size()) progressRound();
    }

    public Player findPlayerById(long userId){
        boolean found = players.stream().anyMatch(player -> player.userId == userId);
        if(!found) return null;
        return players.stream().filter(x -> x.userId == userId).findFirst().get();
    }

    public void handleFold(long userId){
        Player player = findPlayerById(userId);
        if(player == null) return;
        player.fold();
        progressPlayer();
    }

    public void handleCall(long userId, int balance) {
        handleCallOrRaise(userId, balance);
        progressPlayer();
    }

    public void handleRaise(long userId, int balance) {
        handleCallOrRaise(userId, balance);
        do {
            playersTurn = (playersTurn + 1) % players.size();
            haveNotRaised++;
        } while (!players.get(playersTurn).isActive);

        haveNotRaised = 1; // Reset the raise flag since the round progresses after a raise
        // notify update
    }

    private void handleCallOrRaise(long userId, int balance) {
        Player player = findPlayerById(userId);
        if (player == null) return;
        boolean successful = player.call(balance);

        if (successful) {
            potSize += balance;
            roundBet = Math.max(balance, roundBet);
        }
    }
}
