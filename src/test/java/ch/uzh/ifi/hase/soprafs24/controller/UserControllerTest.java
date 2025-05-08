package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.HttpHeaders;

import ch.uzh.ifi.hase.soprafs24.rest.dto.UserProfileUpdatePutDTO;

import java.time.LocalDate;



/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;


  @MockBean
  private UserRepository userRepository;

  @Test
  void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    // given
    User user = new User();
    user.setPassword("securePassword");
    user.setUsername("firstname@lastname");
    user.setStatus(UserStatus.OFFLINE);
    user.setToken("1");
    user.setCreationDate();
    LocalDate birthDate = LocalDate.of(2002, 9, 12);
    user.setBirthDate(birthDate);


    List<User> allUsers = Collections.singletonList(user);

    // this mocks the UserService -> we define above what the userService should return when getUsers() is called
    given(userService.getUsers()).willReturn(allUsers);

    // when
    MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer 1");

    // then
    mockMvc.perform(getRequest).andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].username", is(user.getUsername())))
        .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())))
        .andExpect(jsonPath("$[0].creationDate", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+")))
        .andExpect(jsonPath("$[0].birthDate", is(user.getBirthDate().toString())));
  }

  @Test
  void createUser_validInput_userCreated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setPassword("securePassword");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setPassword("securePassword");
    userPostDTO.setUsername("testUsername");

    given(userService.createUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
        .andExpect(jsonPath("$.token", is(user.getToken())));
  }

  // 1. POST /users (409 Conflict) - Creating a user with existing username
  @Test
  void createUser_duplicateUsername_throwsException() throws Exception {
      // given
      UserPostDTO userPostDTO = new UserPostDTO();
      userPostDTO.setPassword("testPassword");
      userPostDTO.setUsername("testUsername");

      // Setup service to throw conflict exception
      given(userService.createUser(Mockito.any()))
          .willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists"));

      // when/then -> do the request + validate the result
      MockHttpServletRequestBuilder postRequest = post("/users")
          .contentType(MediaType.APPLICATION_JSON)
          .content(asJsonString(userPostDTO));

      // then
      mockMvc.perform(postRequest)
          .andExpect(status().isConflict());
  }

  // 2. GET /users/{userId} (200 OK) - Retrieve user profile with userId
  @Test
  void getUser_validId_userReturned() throws Exception {
      // given
      User user = new User();
      user.setId(1L);
      user.setUsername("testUser");
      user.setStatus(UserStatus.ONLINE);
      user.setCreationDate();
      LocalDate birthDate = LocalDate.of(2002, 9, 12);
      user.setBirthDate(birthDate);

      // Setup service to return user when getUserById is called
      given(userService.getUserById(1L)).willReturn(user);

      // when/then
      MockHttpServletRequestBuilder getRequest = get("/users/1")
          .contentType(MediaType.APPLICATION_JSON);

      // then
      mockMvc.perform(getRequest)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.username", is(user.getUsername())))
          .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
          .andExpect(jsonPath("$.creationDate", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+")))
          .andExpect(jsonPath("$.birthDate", is(user.getBirthDate().toString())));
  }

  // 3. GET /users/{userId} (404 Not Found) - User with userId not found
  @Test
  void getUser_invalidId_notFound() throws Exception {
      // given
      long nonExistentUserId = 99L;

      // Setup service to throw not found exception
      given(userService.getUserById(nonExistentUserId))
          .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

      // when/then
      MockHttpServletRequestBuilder getRequest = get("/users/" + nonExistentUserId)
          .contentType(MediaType.APPLICATION_JSON);

      // then
      mockMvc.perform(getRequest)
          .andExpect(status().isNotFound());
  }

  // 4. PUT /users/{userId} (204 No Content) - Update user profile
  @Test
  void updateUser_validInput_noContent() throws Exception {
      // given
      UserProfileUpdatePutDTO userProfileUpdatePutDTO = new UserProfileUpdatePutDTO();
      userProfileUpdatePutDTO.setUsername("updatedUsername");
      userProfileUpdatePutDTO.setStatus(UserStatus.ONLINE);
      
      User existingUser = new User();
      existingUser.setId(1L);
      existingUser.setToken("1");
      existingUser.setUsername("originalUsername");
      existingUser.setStatus(UserStatus.OFFLINE);
      
      User updatedUser = new User();
      updatedUser.setId(1L);
      updatedUser.setToken("1");  // Important: include token
      updatedUser.setUsername("updatedUsername");
      updatedUser.setStatus(UserStatus.ONLINE);

      // Mock the findUserById call that happens during token validation
      given(userService.findUserById(1L)).willReturn(existingUser);
      
      // Mock the updateUserProfile method call
      given(userService.updateUserProfile(Mockito.any(), Mockito.eq(1L))).willReturn(updatedUser);

      // when/then
      MockHttpServletRequestBuilder putRequest = put("/users/1")
          .contentType(MediaType.APPLICATION_JSON)
          .content(asJsonString(userProfileUpdatePutDTO))
          .header(HttpHeaders.AUTHORIZATION, "Bearer 1");

      // then
      mockMvc.perform(putRequest)
          .andExpect(status().isNoContent());
  }

  // 5. PUT /users/{userId} (404 Not Found) - Update non-existent user
  @Test
  void updateUser_invalidId_notFound() throws Exception {
      long nonExistentUserId = 99L;
      UserProfileUpdatePutDTO userProfileUpdatePutDTO = new UserProfileUpdatePutDTO();
      userProfileUpdatePutDTO.setUsername("updatedUsername");

      User mockUserForAuthCheck = new User();
      mockUserForAuthCheck.setId(nonExistentUserId);
      mockUserForAuthCheck.setToken("1"); // Must match the Bearer token
      
      given(userService.findUserById(nonExistentUserId)).willReturn(mockUserForAuthCheck);
      
      given(userService.updateUserProfile(Mockito.any(), Mockito.eq(nonExistentUserId)))
          .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

      MockHttpServletRequestBuilder putRequest = put("/users/" + nonExistentUserId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(asJsonString(userProfileUpdatePutDTO))
          .header(HttpHeaders.AUTHORIZATION, "Bearer 1");

      mockMvc.perform(putRequest)
          .andExpect(status().isNotFound());
  }

  /**
   * Helper Method to convert userPostDTO into a JSON string such that the input
   * can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   * 
   * @param object
   * @return string
   */
  private String asJsonString(final Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("The request body could not be created.%s", e.toString()));
    }
  }

}