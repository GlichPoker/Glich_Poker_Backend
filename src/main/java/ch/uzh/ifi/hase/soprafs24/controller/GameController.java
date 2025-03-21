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
    public GameModel createGame(@RequestBody CreateGameRequest request) {
        User user = getValidUser(request.userId());
        Player owner = new Player(user.getId(), user.getUsername(), request.gameSettings().initialBalance());
        Game game = new Game(owner, request.gameSettings());
        games.put(game.getSessionId(), game);
        return game.getGameModel(user.getId());
    }

    @PostMapping("/invite")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public boolean invitePlayer(@RequestBody GameActionRequest request) {
        Game game = getValidGame(request.sessionId());
        User user = getValidUser(request.userId());
        if (game.containsUser(request.userId())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Player already part of game");
        game.addPlayer(new Player(user.getId(), user.getUsername(), game.getSettings().initialBalance()));
        // save a request with requestid, sessionId, userID such that the user has possibility to deny
        return true;
    }

    @PostMapping("/denyInvitation")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public boolean denyInvitation(@RequestBody DenyInvitationRequest request) {
        Game game = getValidGame(request.sessionId());
        getValidUser(request.userId());
        if (!game.containsUser(request.userId())) throw new ResponseStatusException(HttpStatus.CONFLICT, "User was not invited to the game");
        game.removePlayer(request.userId());
        return true;
    }

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public GameModel joinGame(@RequestBody GameActionRequest request) {
        Game game = getValidGame(request.sessionId());
        getValidUser(request.userId());
        if (!game.containsUser(request.userId())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Player was not invited to the game");
        game.joinSession(request.userId());
        return game.getGameModel(request.userId());
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public RoundModel startGame(@RequestBody GameActionRequest request) {
        Game game = getValidGame(request.sessionId());
        getValidUser(request.userId());
        if(game.getOwnerId() != request.userId()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only host can start");
        game.startRound();
        return game.getRoundModel(request.userId());
    }

    @PostMapping("/fold")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public RoundModel foldGame(@RequestBody GameActionRequest request) {
        Game game = getValidGame(request.sessionId());
        getValidUser(request.userId());
        isUserPartOfGame(request.userId(), game);
        game.getRound().handleFold(request.userId());
        return game.getRoundModel(request.userId());
    }

    @PostMapping("/call")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public RoundModel callGame(@RequestBody GameActionRequest request) {
        Game game = getValidGame(request.sessionId());
        getValidUser(request.userId());
        isUserPartOfGame(request.userId(), game);
        game.getRound().handleCall(request.userId(), request.amount());
        return game.getRoundModel(request.userId());
    }

    @PostMapping("/raise")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public RoundModel raiseGame(@RequestBody GameActionRequest request) {
        Game game = getValidGame(request.sessionId());
        getValidUser(request.userId());
        isUserPartOfGame(request.userId(), game);
        game.getRound().handleRaise(request.userId(), request.amount());
        return game.getRoundModel(request.userId());
    }

    @PostMapping("/leave")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public boolean leaveGame(@RequestBody GameActionRequest request) {
        Game game = getValidGame(request.sessionId());
        getValidUser(request.userId());
        Player player = isUserPartOfGame(request.userId(), game);
        isPlayerOnline(player);
        player.setIsOnline(false);
        return true;
    }

    @PostMapping("/roundComplete")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public boolean completeRound(@RequestBody GameActionRequest request) {
        Game game = getValidGame(request.sessionId());
        game.roundComplete();
        return true;
    }
}
