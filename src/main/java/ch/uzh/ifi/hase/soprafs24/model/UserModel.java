package ch.uzh.ifi.hase.soprafs24.model;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;

import java.time.LocalDate;

public class UserModel {
    private String username;
    private UserStatus status;
    private LocalDate birthdDate;
    public UserModel(User user) {
        username = user.getUsername();
        status = user.getStatus();
        birthdDate = user.getBirthDate();
    }
    public String getUsername() {return username;}
}
