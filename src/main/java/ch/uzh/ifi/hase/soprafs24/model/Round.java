package ch.uzh.ifi.hase.soprafs24.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

public class Round {
    private List<Player> players;
    protected long potSize;
    protected long roundBet;
    private final Dealer dealer;
    private int playersTurn;
    private final int startPlayer;
    private int betState;
    private final GameSettings gameSettings;
    private int haveNotRaised;
    protected List<Card> communityCards;

    public Round(ArrayList<Player> players, int startPlayer, boolean isTest, GameSettings gameSettings) {
        this.players = players;
        this.dealer = new Dealer(new Deck());
        this.startPlayer = startPlayer;
        playersTurn = startPlayer;
        betState = 0;
        potSize = 0;
        haveNotRaised = 0;
        communityCards = new ArrayList<>();
        roundBet = 0;
        this.gameSettings = gameSettings;

        if (!isTest) dealPlayers(); // deal with this for tests
        // notify update
    }

    public Round(ArrayList<Player> players, int startPlayer, GameSettings gameSettings) {
        this(players, startPlayer,false, gameSettings);
    }

    public void onRoundCompletion(){
        List<Player> winners = roundComplete();
        Map<Player, Double> winnings = calculateWinnings(winners);
        players = updateBalances(winnings);
        resetPlayers();
        // notify client update
    }

    private void resetPlayers(){
        for(Player player : players){
            player.reset();
        }
    }

    private ArrayList<Player> updateBalances(Map<Player, Double> winnings) {
        for(Map.Entry<Player, Double> entry : winnings.entrySet()) {
            Player player = entry.getKey();
            player.increaseBalance(entry.getValue());
        }
        return new ArrayList<>(winnings.keySet().stream().toList());
    }

    public Map<Player, Double> calculateWinnings(List<Player> winners){
        Map<Player, Double> winnings = new HashMap<>();
        for(Player winner : winners){
            double amount = (double)winner.getRoundBet() / this.potSize * potSize;
            winnings.put(winner, amount);
        }
        return winnings;
    }

    public List<Player> roundComplete(){
    List<Player> winners = new ArrayList<>();
    EvaluationResult winner = null;
    for (Player player : players) {
        ArrayList<Card> mergedCards = mergeHands(player.getHand());

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
        while(!players.get(playersTurn).isActive());
        // notify update
        if(haveNotRaised == players.size()) progressRound();
    }

    public Player findPlayerById(long userId){
        boolean found = players.stream().anyMatch(player -> player.getUserId() == userId);
        if(!found) return null;
        return players.stream().filter(x -> x.getUserId() == userId).findFirst().get();
    }

    public void handleFold(long userId){
        Player player = findPlayerById(userId);
        if(player == null) return;
        if (!player.isActive()) throw new ResponseStatusException(HttpStatus.CONFLICT, "user already folded");
        player.fold();
        progressPlayer();
    }

    public void handleCall(long userId, long balance) {
        handleCallOrRaise(userId, balance);
        progressPlayer();
    }

    public void handleRaise(long userId, long balance) {
        handleCallOrRaise(userId, balance);
        do {
            playersTurn = (playersTurn + 1) % players.size();
            haveNotRaised++;
        } while (!players.get(playersTurn).isActive());

        haveNotRaised = 1; // Reset the raise flag since the round progresses after a raise
        // notify update
    }

    private void handleCallOrRaise(long userId, long balance) {
        Player player = findPlayerById(userId);
        if (player == null) return;
        if (!player.isActive()) throw new ResponseStatusException(HttpStatus.CONFLICT, "user already folded");
        boolean successful = player.call(balance);

        if (successful) {
            potSize += balance;
            roundBet = Math.max(balance, roundBet);
        }
    }
}
