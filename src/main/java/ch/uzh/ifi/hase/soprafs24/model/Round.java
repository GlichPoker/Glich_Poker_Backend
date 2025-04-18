package ch.uzh.ifi.hase.soprafs24.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.IntStream;

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
    private RoundCompletionListener roundCompletionListener;

    public Round(List<Player> players, int startPlayer, boolean isTest, GameSettings gameSettings) {
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
        handleBlinds();
        // notify update
    }

    public void setRoundCompletionListener(RoundCompletionListener roundCompletionListener) {
        this.roundCompletionListener = roundCompletionListener;
    }

    public Round(List<Player> players, int startPlayer, GameSettings gameSettings) {
        this(players, startPlayer,false, gameSettings);
    }

    public void onRoundCompletion(){
        List<Player> winners = roundComplete();
        Map<Player, Double> winnings = calculateWinnings(winners);
        players = updateBalances(winnings);
        resetPlayers();

        if (roundCompletionListener != null) {
            roundCompletionListener.onRoundComplete(winners);
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No RoundCompletionListener set");
        }
    }

    public void handleBlinds(){
        // happens only in tests bc i am lazy
        if(gameSettings == null) return;
        Player bigBlind = players.get((playersTurn - 1 + players.size()) % players.size());
        Player smallBlind = players.get((playersTurn - 2 + players.size()) % players.size());
        bigBlind.call(gameSettings.bigBlind());
        smallBlind.call(gameSettings.smallBlind());
        roundBet = gameSettings.bigBlind();
    }

    private void resetPlayers(){
        for(Player player : players){
            player.reset();
        }
    }

    private List<Player> updateBalances(Map<Player, Double> winnings) {
        for(Map.Entry<Player, Double> entry : winnings.entrySet()) {
            Player player = entry.getKey();
            player.increaseBalance(entry.getValue());
        }
        return new ArrayList<>(winnings.keySet().stream().toList());
    }

    public List<PlayerModel> getPlayerModelsOfOtherParticipants(long userId){
        int playerIdx = IntStream.range(0, players.size())
                .filter(i -> players.get(i).getUserId() == userId)
                .findFirst()
                .orElse(-1);
        if(playerIdx == -1)throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found");
        List<Player> otherPlayers = new ArrayList<>();
        otherPlayers.addAll(players.subList(playerIdx + 1, players.size()));
        otherPlayers.addAll(players.subList(0, playerIdx));
        ArrayList<PlayerModel> models = new ArrayList<>();
        otherPlayers.forEach(player -> models.add(new PlayerModel(player)));
        return models;
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
        List<Card> mergedCards = mergeHands(player.getHand());

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

    private List<Card> mergeHands(Card[] hand){
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
        if(player == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found");
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

    public void handleCheck(long userId){
        Player player = findPlayerById(userId);
        if(player == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "player not found");
        if (!player.isActive()) throw new ResponseStatusException(HttpStatus.CONFLICT, "player already folded");
        if(player.getRoundBet() < roundBet) throw new ResponseStatusException(HttpStatus.CONFLICT, "cannot check if the player has bet less than the current bet amount");
        progressPlayer();
    }

    private void handleCallOrRaise(long userId, long balance) {
        Player player = findPlayerById(userId);
        if (player == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "player not found");
        if (!player.isActive()) throw new ResponseStatusException(HttpStatus.CONFLICT, "player already folded");
        long difference = player.calculateDifference(balance);
        boolean successful = player.call(difference);

        if (successful) {
            potSize += difference;
            roundBet = Math.max(balance, roundBet);
        }
    }
    public GameSettings getGameSettings() {return gameSettings;}
    public int getPlayersTurn(){return playersTurn;}
    public int getStartPlayer(){return startPlayer;}
    public List<Player> getPlayers(){return players;}
}
