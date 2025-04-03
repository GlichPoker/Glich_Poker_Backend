package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.model.*;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.GameSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.ConcurrentHashMap;

@RestController
public class GameController {

    // TODO: push update to all clients of session after every action
    private final ConcurrentHashMap<Long, ch.uzh.ifi.hase.soprafs24.model.Game> activeGames = new ConcurrentHashMap<>();
    private final GameService gameService;
    private final GameSettingsService gameSettingsService;

    @Autowired
    public GameController(GameService gameService, GameSettingsService gameSettingsService) {
        this.gameService = gameService;
        this.gameSettingsService = gameSettingsService;
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Game createGame(@RequestBody CreateGameRequest request) {
        // Delegate game creation to the GameService
        ch.uzh.ifi.hase.soprafs24.entity.GameSettings settings = this.gameSettingsService.createGameSettings(request.gameSettings());
        return gameService.createGame(request.userId(), settings.getId());
    }

    @PostMapping("/invite")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public boolean invitePlayer(@RequestBody GameActionRequest request) {
        Game game = gameService.getGameBySessionId(request.sessionId());
        gameService.addPlayerToGame(game, request.userId(), game.getSettings().getInitialBalance());
        return true;
    }

    @PostMapping("/denyInvitation")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public boolean denyInvitation(@RequestBody DenyInvitationRequest request) {
        Game game = gameService.getGameBySessionId(request.sessionId());
        gameService.removePlayerFromGame(game, request.userId());
        return true;
    }

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Game joinGame(@RequestBody GameActionRequest request) {
        Game game = gameService.getGameBySessionId(request.sessionId());
        gameService.addPlayerToGame(game,request.userId(), game.getSettings().getInitialBalance());
        return game;
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public RoundModel startGame(@RequestBody GameActionRequest request) {
        Game game = gameService.getGameBySessionId(request.sessionId());
        gameService.startRound(game);
        ch.uzh.ifi.hase.soprafs24.model.Game newGame = new ch.uzh.ifi.hase.soprafs24.model.Game(game);
        activeGames.put(request.sessionId(), newGame);
        return activeGames.get(request.sessionId()).getGameModel(request.userId()).getRound();
    }

    @PostMapping("/fold")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public RoundModel foldGame(@RequestBody GameActionRequest request) {
        ch.uzh.ifi.hase.soprafs24.model.Game game = activeGames.get(request.sessionId());
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        game.getRound().handleFold(request.userId());
        return game.getRoundModel(request.userId());
    }

    @PostMapping("/roundComplete")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ch.uzh.ifi.hase.soprafs24.model.Game completeRound(@RequestBody GameActionRequest request) {
        ch.uzh.ifi.hase.soprafs24.model.Game game = activeGames.get(request.sessionId());
        gameService.completeRound(game);
        return game;
    }


    @PostMapping("/call")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public RoundModel callGame(@RequestBody GameActionRequest request) {
        ch.uzh.ifi.hase.soprafs24.model.Game game = activeGames.get(request.sessionId());
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        game.getRound().handleCall(request.userId(), request.amount());
        return game.getRoundModel(request.userId());
    }

    @PostMapping("/raise")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public RoundModel raiseGame(@RequestBody GameActionRequest request) {
        ch.uzh.ifi.hase.soprafs24.model.Game game = activeGames.get(request.sessionId());
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        game.getRound().handleRaise(request.userId(), request.amount());
        return game.getRoundModel(request.userId());
    }

    @PostMapping("/leave")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public boolean leaveGame(@RequestBody GameActionRequest request) {
        Game game = gameService.getGameBySessionId(request.sessionId());
        gameService.setPlayerOffline(game, request.userId());
        return true;
    }

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public boolean saveGame(@RequestParam long sessionId) {
        Game game = gameService.getGameBySessionId(sessionId);
        activeGames.remove(sessionId);
        gameService.saveSession(game);
        return true;
    }
}
