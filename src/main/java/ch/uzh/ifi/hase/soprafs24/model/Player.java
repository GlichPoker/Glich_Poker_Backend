package ch.uzh.ifi.hase.soprafs24.model;

public class Player {
    public long userId;
    public String name;
    public double balance;
    public boolean isActive;
    public Card[] hand;
    public long roundBet;

    public Player(long userId, String name, long balance){
        this.userId = userId;
        this.name = name;
        this.balance = balance;
        this.isActive = true;
        this.hand = new Card[2];
        this.roundBet = 0;
    }

    public void startNewRound(){
        roundBet = 0;
        hand = new Card[2];
        isActive = true;
    }

    public void fold(){
        isActive = false;
    }

    // can be used for raise, call and check since its called from controller and there we decide on action not here
    public boolean call(long amount){
        if(amount < balance && isActive){
            balance -= amount;
            roundBet += amount;
        }
        else{
            isActive = false;
        }
        return isActive;
    }
}
