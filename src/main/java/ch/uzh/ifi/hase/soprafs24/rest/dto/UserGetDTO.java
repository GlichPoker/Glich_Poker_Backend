package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

public class UserGetDTO {

  private Long id;
  private String username;
  private UserStatus status;
  private LocalDateTime creationDate;
  private LocalDate birthDate;

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

  public void setCreationDate(LocalDateTime creationDate){
    this.creationDate = creationDate;
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
}
