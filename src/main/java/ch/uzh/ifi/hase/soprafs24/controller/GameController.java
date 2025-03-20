package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.*;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import java.util.concurrent.ConcurrentHashMap;
@CrossOrigin(origins = "*")
@RestController
public class GameController {
    private final ConcurrentHashMap<Long, Game> games;
    private final UserService userService;

    public GameController(UserService userService) {
        this.games = new ConcurrentHashMap<>();
        this.userService = userService;
    }

    private Game getValidGame(long sessionId) {
        Game game = games.get(sessionId);
        if (game == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        return game;
    }

    private User getValidUser(long userId) {
        User user = userService.getUserById(userId);
        if (user == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        return user;
    }

    private Player isUserPartOfGame(long userId, Game game) {
        if(!game.containsUser(userId)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not part of game");
        return game.getPlayer(userId);
    }

    private void isPlayerOnline(Player player) {
        if(!player.isOnline()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot leave if not part of game");
    }


    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Long createGame(@RequestBody CreateGameRequest request) {
        User user = getValidUser(request.getUserId());
        Player owner = new Player(user.getId(), user.getUsername(), request.getGameSettings().getInitialBalance());
        Game game = new Game(owner, request.getGameSettings());
        games.put(game.getSessionId(), game);
        return game.getSessionId();
    }

    @PostMapping("/invite")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public boolean invitePlayer(@RequestBody GameActionRequest request) {
        Game game = getValidGame(request.getSessionId());
        User user = getValidUser(request.getUserId());
        if (game.containsUser(request.getUserId())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Player already part of game");
        game.addPlayer(new Player(user.getId(), user.getUsername(), game.getSettings().getInitialBalance()));
        return true;
    }

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public boolean joinGame(@RequestBody GameActionRequest request) {
        Game game = getValidGame(request.getSessionId());
        getValidUser(request.getUserId());
        if (!game.containsUser(request.getUserId())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Player was not invited to the game");
        game.joinSession(request.getUserId());
        return true;
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public boolean startGame(@RequestBody GameActionRequest request) {
        Game game = getValidGame(request.getSessionId());
        getValidUser(request.getUserId());
        if(game.getOwnerId() != request.getUserId()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only host can start");
        game.startRound();
        return true;
    }

    @PostMapping("/fold")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public boolean foldGame(@RequestBody GameActionRequest request) {
        Game game = getValidGame(request.getSessionId());
        getValidUser(request.getUserId());
        isUserPartOfGame(request.getUserId(), game);
        game.getRound().handleFold(request.getUserId());
        return true;
    }

    @PostMapping("/call")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public boolean callGame(@RequestBody GameActionRequest request) {
        Game game = getValidGame(request.getSessionId());
        getValidUser(request.getUserId());
        isUserPartOfGame(request.getUserId(), game);
        game.getRound().handleCall(request.getUserId(), request.getAmount());
        return true;
    }

    @PostMapping("/raise")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public boolean raiseGame(@RequestBody GameActionRequest request) {
        Game game = getValidGame(request.getSessionId());
        getValidUser(request.getUserId());
        isUserPartOfGame(request.getUserId(), game);
        game.getRound().handleRaise(request.getUserId(), request.getAmount());
        return true;
    }

    @PostMapping("/leave")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public boolean leaveGame(@RequestBody GameActionRequest request) {
        Game game = getValidGame(request.getSessionId());
        getValidUser(request.getUserId());
        Player player = isUserPartOfGame(request.getUserId(), game);
        isPlayerOnline(player);
        player.setIsOnline(false);
        return true;
    }

    @PostMapping("/roundComplete")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public boolean completeRound(@RequestBody GameActionRequest request) {
        Game game = getValidGame(request.getSessionId());
        game.roundComplete();
        return true;
    }
}
