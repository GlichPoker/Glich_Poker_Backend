package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.HandRank;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;

@Entity
public class GameSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private long initialBalance;
    @Column
    private long smallBlind;
    @Column
    private long bigBlind;
    @Column(columnDefinition = "VARCHAR(64)")
    private String ordering;
    @Column
    private boolean descending;

    public GameSettings(long initialBalance, long smallBlind, long bigBlind, List<HandRank> order, boolean descending) {
        this.initialBalance = initialBalance;
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
        setOrder(order);
        this.descending = descending;
    }

    public GameSettings() {}

    public long getId() { return id; }
    public long getInitialBalance() { return initialBalance; }
    public long getSmallBlind() { return smallBlind; }
    public long getBigBlind() { return bigBlind; }
    public List<HandRank> getOrder(){
        return Arrays.stream(ordering.split(";"))
                .map(Integer::parseInt)
                .map(x -> HandRank.values()[x])
                .toList();
    }
    public void setOrder(List<HandRank> orders) {
        ordering = String.join(";", orders.stream()
                .map(x -> x.value)
                .map(String::valueOf)
                .toList());
    }
    public boolean isDescending() { return descending; }
    public void setDescending(boolean descending) { this.descending = descending; }
    public void setInitialBalance(long initialBalance) { this.initialBalance = initialBalance; }
    public void setSmallBlind(long smallBlind) { this.smallBlind = smallBlind; }
    public void setBigBlind(long bigBlind) { this.bigBlind = bigBlind; }

    public ch.uzh.ifi.hase.soprafs24.model.GameSettings toModel() {
        return new ch.uzh.ifi.hase.soprafs24.model.GameSettings(initialBalance, smallBlind, bigBlind, getOrder(), descending);
    }
}
