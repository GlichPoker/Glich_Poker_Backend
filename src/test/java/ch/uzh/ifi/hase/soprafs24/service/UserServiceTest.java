package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private GameService gameService;

  @InjectMocks
  private UserService userService;

  private User testUser;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);

    // given
    testUser = new User();
    testUser.setId(1L);
    testUser.setPassword("securePassword");
    testUser.setUsername("testUsername");
    testUser.setToken("testToken");
    testUser.setStatus(UserStatus.OFFLINE);
    testUser.setCreationDate();
    testUser.setBirthDate(LocalDate.of(2000, 1, 1));

    Mockito.when(userRepository.save(Mockito.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(testUser);
    Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    Mockito.when(userRepository.findByToken("testToken")).thenReturn(testUser);
  }

  @Test
  void createUser_duplicateUsername_throwsException() {
    // given
    User duplicateUser = new User();
    duplicateUser.setUsername("testUsername");
    duplicateUser.setPassword("anotherPassword");

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.createUser(duplicateUser));
    assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    assertTrue(exception.getReason().contains("username"));
  }

  @Test
  void loginUser_validCredentials_success() {
    // given
    User loginAttemptUser = new User();
    loginAttemptUser.setUsername("testUsername");
    loginAttemptUser.setPassword("securePassword");

    // when
    User loggedInUser = userService.loginUser(loginAttemptUser);

    // then
    assertNotNull(loggedInUser);
    assertEquals(testUser.getId(), loggedInUser.getId());
    assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
    Mockito.verify(userRepository, Mockito.times(1)).save(loggedInUser);
  }

  @Test
  void loginUser_userNotFound_throwsRuntimeException() {
    // given
    User loginAttemptUser = new User();
    loginAttemptUser.setUsername("nonExistentUser");
    loginAttemptUser.setPassword("anyPassword");

    Mockito.when(userRepository.findByUsername("nonExistentUser")).thenReturn(null);

    // when / then
    RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.loginUser(loginAttemptUser));
    assertEquals("User not found.", exception.getMessage());
  }

  @Test
  void loginUser_invalidPassword_throwsRuntimeException() {
    // given
    User loginAttemptUser = new User();
    loginAttemptUser.setUsername("testUsername");
    loginAttemptUser.setPassword("wrongPassword");

    // when / then
    RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.loginUser(loginAttemptUser));
    assertEquals("Invalid credentials.", exception.getMessage());
  }

  @Test
  void logoutUser_userExists_setsStatusOffline() {
    // given
    testUser.setStatus(UserStatus.ONLINE);
    Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(testUser);

    // when
    userService.logoutUser("testUsername");

    // then
    assertEquals(UserStatus.OFFLINE, testUser.getStatus());
    Mockito.verify(userRepository, Mockito.times(1)).save(testUser);
  }

  @Test
  void logoutUser_userNotExists_doesNothing() {
    // given
    Mockito.when(userRepository.findByUsername("nonExistentUser")).thenReturn(null);

    // when
    userService.logoutUser("nonExistentUser");

    // then
    Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
  }

  @Test
  void getUserById_userExists_returnsUser() {
    // when
    User foundUser = userService.getUserById(1L);

    // then
    assertNotNull(foundUser);
    assertEquals(testUser.getId(), foundUser.getId());
    Mockito.verify(gameService, times(1)).getGamesOwnedByUser(1L);
  }

  @Test
  void getUserById_userNotExists_throwsNotFound() {
    // given
    Long nonExistentId = 99L;
    Mockito.when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // when / then
    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.getUserById(nonExistentId));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    assertTrue(exception.getReason().contains("User not found with ID: " + nonExistentId));
  }

  @Test
  void findUserById_userExists_returnsUser() {
    // when
    User foundUser = userService.findUserById(1L);

    // then
    assertNotNull(foundUser);
    assertEquals(testUser.getId(), foundUser.getId());
  }

  @Test
  void findUserById_userNotExists_returnsNull() {
    // given
    Long nonExistentId = 99L;
    Mockito.when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // when
    User foundUser = userService.findUserById(nonExistentId);

    // then
    assertNull(foundUser);
  }

  @Test
  void findUserByToken_userExists_returnsUser() {
    // when
    User foundUser = userService.findUserByToken("testToken");

    // then
    assertNotNull(foundUser);
    assertEquals(testUser.getToken(), foundUser.getToken());
  }

  @Test
  void findUserByToken_userNotExists_returnsNull() {
    // given
    Mockito.when(userRepository.findByToken("nonExistentToken")).thenReturn(null);

    // when
    User foundUser = userService.findUserByToken("nonExistentToken");

    // then
    assertNull(foundUser);
  }

  @Test
  void updateUserProfile_userExists_updatesFields() {
    // given
    User updateData = new User();
    updateData.setUsername("updatedUsername");
    updateData.setBirthDate(LocalDate.of(1995, 5, 5));
    updateData.setStatus(UserStatus.ONLINE);

    // when
    User updatedUser = userService.updateUserProfile(updateData, 1L);

    // then
    assertNotNull(updatedUser);
    assertEquals(1L, updatedUser.getId());
    assertEquals("updatedUsername", updatedUser.getUsername());
    assertEquals(LocalDate.of(1995, 5, 5), updatedUser.getBirthDate());
    assertEquals(UserStatus.ONLINE, updatedUser.getStatus());
    assertEquals(testUser.getPassword(), updatedUser.getPassword());
    assertEquals(testUser.getToken(), updatedUser.getToken());
    Mockito.verify(userRepository, Mockito.times(1)).save(testUser);
  }

  @Test
  void updateUserProfile_userExists_partialUpdate_updatesOnlyProvidedFields() {
    // given
    User updateData = new User();
    updateData.setUsername("newUsernameOnly");

    // when
    User updatedUser = userService.updateUserProfile(updateData, 1L);

    // then
    assertNotNull(updatedUser);
    assertEquals("newUsernameOnly", updatedUser.getUsername());
    assertEquals(testUser.getBirthDate(), updatedUser.getBirthDate());
    assertEquals(UserStatus.OFFLINE, updatedUser.getStatus());
    Mockito.verify(userRepository, Mockito.times(1)).save(testUser);
  }

  @Test
  void getAllUsers_returnsListOfUsers() {
    // given
    List<User> users = Collections.singletonList(testUser);
    Mockito.when(userRepository.findAll()).thenReturn(users);

    // when
    List<User> result = userService.getAllUsers();

    // then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(testUser.getUsername(), result.get(0).getUsername());
  }

  @Test
  void saveUser_callsRepositorySave() {
    // given
    User userToSave = new User();
    userToSave.setUsername("saveMe");

    // when
    userService.saveUser(userToSave);

    // then
    Mockito.verify(userRepository, times(1)).save(userToSave);
  }
}
