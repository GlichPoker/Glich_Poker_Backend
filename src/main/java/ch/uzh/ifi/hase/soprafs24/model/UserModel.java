package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;

import java.time.LocalDate;

public class UserModel {
    private long id;
    private String username;
    private UserStatus status;
    private LocalDate birthdDate;

    public UserModel(User user) {
        id = user.getId();
        username = user.getUsername();
        status = user.getStatus();
        birthdDate = user.getBirthDate();
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
    public UserStatus getStatus() {return status;}
    public LocalDate getBirthdDate() {return birthdDate;}
}
