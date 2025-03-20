package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserProfileUpdatePutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserLoginGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@CrossOrigin(origins = "*")
@RestController
public class UserController {

  private final UserService userService;

  UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UserGetDTO> getAllUsers() {
    // fetch all users in the internal representation
    List<User> users = userService.getUsers();
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    // convert each user to the API representation
    for (User user : users) {
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }
    return userGetDTOs;
  }

  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public UserLoginGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // create user
    User createdUser = userService.createUser(userInput);
    // convert internal representation of user back to API
    return DTOMapper.INSTANCE.convertEntityToUserLoginGetDTO(createdUser);
  }

  @PostMapping("/users/login")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserLoginGetDTO loginUser(@RequestBody UserPostDTO userPostDTO) {
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    User logedInUser = userService.loginUser(userInput);

    // convert internal representation of user back to API
    return DTOMapper.INSTANCE.convertEntityToUserLoginGetDTO(logedInUser);
  }

  @GetMapping("/users/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public UserGetDTO getUser(@PathVariable("userId") Long userId) {
    User userById = userService.getUserById(userId);
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(userById);
  }

  @PutMapping("/users/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public UserGetDTO updateUserProfile(
      @PathVariable("userId") Long userId, 
      @RequestBody UserProfileUpdatePutDTO userProfileUpdatePutDTO,
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
      
      String token = null;
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
          token = authHeader.substring(7); // Remove "Bearer " prefix
      }


      if (token == null || !token.equals(userService.findUserById(userId).getToken())) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized to update this user's profile");
      }
      
      User payload = DTOMapper.INSTANCE.convertUserPutDTOToEntity(userProfileUpdatePutDTO);
      User updatedUser = userService.updateUserProfile(payload, userId);
      return DTOMapper.INSTANCE.convertEntityToUserGetDTO(updatedUser);
  }
}
