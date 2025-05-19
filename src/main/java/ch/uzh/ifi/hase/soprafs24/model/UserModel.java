package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.UserLobbyStatus;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;

import java.time.LocalDate;

public class UserModel {
    private Long id;
    private String username;
    private UserStatus status;
    private LocalDate birthDate; // Corrected typo from birthdDate to birthDate
    private UserLobbyStatus userLobbyStatus;
    private Long currentLobbyId;

    public UserModel() {}

    public UserModel(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.status = user.getStatus();
        this.birthDate = user.getBirthDate();
        this.userLobbyStatus = user.getUserLobbyStatus(); 
        this.currentLobbyId = user.getCurrentLobbyId(); 
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) { 
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) { 
        this.username = username;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) { 
        this.status = status;
    }

    public LocalDate getBirthDate() { 
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) { // Added setter for birthDate
        this.birthDate = birthDate;
    }

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
}
