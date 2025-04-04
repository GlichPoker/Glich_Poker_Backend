package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;
  private final GameService gameService;

  @Autowired
  public UserService(@Qualifier("userRepository") UserRepository userRepository, GameService gameService) {
    this.userRepository = userRepository;
    this.gameService = gameService;
  }

  public List<User> getUsers() {
    return this.userRepository.findAll();
  }

  public User createUser(User newUser) {
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.OFFLINE);
    newUser.setCreationDate();
    checkIfUserExists(newUser);
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    newUser = userRepository.save(newUser);
    userRepository.flush();

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

  /**
   * This is a helper method that will check the uniqueness criteria of the
   * username and the name
   * defined in the User entity. The method will do nothing if the input is unique
   * and throw an error otherwise.
   *
   * @param userToBeCreated
   * @throws org.springframework.web.server.ResponseStatusException
   * @see User
   */
  private void checkIfUserExists(User userToBeCreated) {
    User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

    String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
    if (userByUsername != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(baseErrorMessage, "username", "is"));
    }
  }

  public User loginUser(User userInput){
    User existingUser = userRepository.findByUsername(userInput.getUsername());
    if (userInput.getPassword().equals(existingUser.getPassword())){
      return existingUser;
    } else {
      return userInput;
    }
  }

  public User getUserById(Long userId){
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new ResponseStatusException(
        HttpStatus.NOT_FOUND, 
        "User not found with ID: " + userId
      )
    );
    List<Game> games = gameService.getGamesOwnedByUser(userId);
    user.setGames(games);
    return user;
  }

  public User findUserById(Long userId) {
    return userRepository.findById(userId).orElse(null);
  }

  public User findUserByToken(String token){
    return userRepository.findByToken(token);
  }

  public User updateUserProfile(User updatedData, Long userId) {
    User existingUser = findUserById(userId);
    
    // Only update fields that are not null in the input
    if (updatedData.getUsername() != null) {
        existingUser.setUsername(updatedData.getUsername());
    }
    
    if (updatedData.getBirthDate() != null) {
        existingUser.setBirthDate(updatedData.getBirthDate());
    }
    
    if (updatedData.getStatus() != null) {
        existingUser.setStatus(updatedData.getStatus());
    }
    userRepository.save(existingUser);
    return existingUser;
  }

  public List<User> getAllUsersExceptSelf(long userId) {
      return userRepository.getAllUsers(userId);
  }
}
