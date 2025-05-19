package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.UserLobbyStatus;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.model.UserModel;

import java.io.Serial;
import java.time.LocalDateTime;
import java.time.LocalDate;
import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
* Internal User Representation
* This class composes the internal representation of the user and defines how
* the user is stored in the database.
* Every variable will be mapped into a database field with the @Column
* annotation
* - nullable = false -> this cannot be left empty
* - unique = true -> this value must be unqiue across the database -> composes
* the primary key
*/
@Entity
@Table(name = "\"USER\"")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = true)
    private UserLobbyStatus userLobbyStatus;

    @Column(nullable = true)
    private Long currentLobbyId;

    @Column
    private LocalDateTime creationDate;

    @Column
    private LocalDate birthDate;

    @Column
    private int bankruptCount = 0;

    @Column
    private int gameCount = 0;

    @Column
    private long roundCount = 0;

    @Column(nullable = false)
    private float bb_100_record = 0;

    @Column
    private long bb_100_count = 0;

    @Transient
    private List<Game> games;

    public Long getId() {
    return id;
    }

    public void setId(Long id) {
    this.id = id;
    }

    public String getPassword() {
    return password;
    }

    public void setPassword(String password) {
    this.password = password;
    }

    public String getUsername() {
    return username;
    }
    public void setGames(List<Game> games) {this.games = games;}

    public void setUsername(String username) {
    this.username = username;
    }

    public String getToken() {
    return token;
    }

    public void setToken(String token) {
    this.token = token;
    }

    public UserStatus getStatus() {
    return status;
    }

    public void setStatus(UserStatus status) {
    this.status = status;
    }

    public void setCreationDate(){
    this.creationDate = LocalDateTime.now();
    }

    public LocalDateTime getCreationDate(){
    return this.creationDate;
    }

    public void setBirthDate(LocalDate birthDate){
    this.birthDate = birthDate;
    }

    public LocalDate getBirthDate(){
    return this.birthDate;
    }

    public int getBankruptCount() {
        return bankruptCount;
    }

    public void setBankruptCount(int bankruptCount) {
        this.bankruptCount = bankruptCount;
    }

    public int getGameCount() {
        return gameCount;
    }

    public void setGameCount(int gameCount) {
        this.gameCount = gameCount;
    }

    public long getRoundCount() {
        return roundCount;
    }

    public void setRoundCount(long roundCount) {
        this.roundCount = roundCount;
    }

    public float getBB_100_record() {
        return bb_100_record;
    }

    public void setBB_100_record(float bb_100_record) {
        this.bb_100_record = bb_100_record;
    }

    public long getBB_100_count() {
        return bb_100_count;
    }

    public void setBB_100_count(long bb_100_count) {
        this.bb_100_count = bb_100_count;
    }

    public List<Game> getGames() {return games;}

    public UserLobbyStatus getUserLobbyStatus() {
    return userLobbyStatus;
    }

    public void setUserLobbyStatus(UserLobbyStatus userLobbyStatus) {
    this.userLobbyStatus = userLobbyStatus;
    }

    public Long getCurrentLobbyId() {
    return currentLobbyId;
    }

    public void setCurrentLobbyId(Long currentLobbyId) {
    this.currentLobbyId = currentLobbyId;
    }

    public UserModel toUserModel(){
        UserModel model = new UserModel();
        model.setId(this.getId());
        model.setUsername(this.getUsername());
        model.setStatus(this.getStatus());
        model.setUserLobbyStatus(this.getUserLobbyStatus());
        model.setCurrentLobbyId(this.getCurrentLobbyId());
        return model;
    }
}