package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.time.LocalDate;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

public class UserProfileUpdatePutDTO {

  private Long id;
  private String username;
  private UserStatus status;
  private LocalDate birthDate;
  private ch.uzh.ifi.hase.soprafs24.constant.UserLobbyStatus userLobbyStatus;
  private Long currentLobbyId;

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

  public void setBirthDate(LocalDate birthDate){
    this.birthDate = birthDate;
  }

  public LocalDate getBirthDate(){
    return this.birthDate;
  }

  public ch.uzh.ifi.hase.soprafs24.constant.UserLobbyStatus getUserLobbyStatus() {
    return userLobbyStatus;
  }

  public void setUserLobbyStatus(ch.uzh.ifi.hase.soprafs24.constant.UserLobbyStatus userLobbyStatus) {
    this.userLobbyStatus = userLobbyStatus;
  }

  public Long getCurrentLobbyId() {
    return currentLobbyId;
  }

  public void setCurrentLobbyId(Long currentLobbyId) {
    this.currentLobbyId = currentLobbyId;
  }
}
