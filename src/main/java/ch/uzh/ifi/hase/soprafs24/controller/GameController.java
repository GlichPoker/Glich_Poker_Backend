package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.Model;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.*;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.GameSettingsService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.websockets.WS_Handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/game")
public class GameController implements ch.uzh.ifi.hase.soprafs24.model.Game.GameCompletionCallback {

    private final ConcurrentHashMap<Long, ch.uzh.ifi.hase.soprafs24.model.Game> activeGames = new ConcurrentHashMap<>();
    private final GameService gameService;
    private final GameSettingsService gameSettingsService;
    private final UserService userService;
    private final WS_Handler wsHandler;

    @Autowired
    public GameController(GameService gameService, GameSettingsService gameSettingsService, UserService userService,
            WS_Handler wsHandler) {
        this.gameService = gameService;
        this.gameSettingsService = gameSettingsService;
        this.userService = userService;
        this.wsHandler = wsHandler;
    }

    @Override
    public void onGameComplete(ch.uzh.ifi.hase.soprafs24.model.Game gameModel, List<Player> winners) {
        if (gameModel == null) {
            return;
        }

        Game gameEntity = gameService.completeRound(gameModel);

        ch.uzh.ifi.hase.soprafs24.model.Game newGameModel = new ch.uzh.ifi.hase.soprafs24.model.Game(gameEntity, false);
        wsHandler.sendModelToAll(Long.toString(newGameModel.getSessionId()), newGameModel, Model.GAMEMODEL);
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ch.uzh.ifi.hase.soprafs24.model.Game createGame(@RequestBody CreateGameRequest request) {
        ch.uzh.ifi.hase.soprafs24.entity.GameSettings settings = gameSettingsService
                .createGameSettings(request.gameSettings());
        User user = userService.getUserById(request.userId());
        Game newGame = gameService.createGame(user, settings.getId(), request.isPublic());

        ch.uzh.ifi.hase.soprafs24.model.Game gameModel = new ch.uzh.ifi.hase.soprafs24.model.Game(newGame, false);
        wsHandler.sendModelToAll(Long.toString(gameModel.getSessionId()), gameModel, Model.GAMEMODEL);

        return new ch.uzh.ifi.hase.soprafs24.model.Game(newGame, false);
    }

    @PostMapping("/invite")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public boolean invitePlayer(@RequestBody GameActionRequest request) {
        Game game = gameService.getGameBySessionId(request.sessionId());
        User user = userService.getUserById(request.userId());
        gameService.addPlayerToGame(game, user, game.getSettings().getInitialBalance());
        return true;
    }

    @PostMapping("/denyInvitation")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public boolean denyInvitation(@RequestBody DenyInvitationRequest request) {
        Game game = gameService.getGameBySessionId(request.sessionId());
        gameService.removePlayerFromGame(game, request.userId());
        return true;
    }

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ch.uzh.ifi.hase.soprafs24.model.Game joinGame(@RequestBody GameActionRequest request) {
        Game game = gameService.getGameBySessionId(request.sessionId());
        User user = userService.getUserById(request.userId());
        gameService.handlePlayerJoinOrRejoin(game, user);
        Game updatedGame = gameService.getGameBySessionId(request.sessionId());

        ch.uzh.ifi.hase.soprafs24.model.Game gameModel = new ch.uzh.ifi.hase.soprafs24.model.Game(updatedGame, false);
        wsHandler.sendModelToAll(Long.toString(gameModel.getSessionId()), gameModel, Model.GAMEMODEL);

        return gameModel;
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public RoundModel startGame(@RequestBody GameActionRequest request) {
        Game game = gameService.getGameBySessionId(request.sessionId());

        gameService.startRound(game);

        ch.uzh.ifi.hase.soprafs24.model.Game newGame = new ch.uzh.ifi.hase.soprafs24.model.Game(game, true);

        activeGames.put(request.sessionId(), newGame);

        wsHandler.sendGameStateToAll(Long.toString(request.sessionId()), "IN_GAME");

        wsHandler.sendModelToAll(Long.toString(newGame.getSessionId()), newGame, Model.ROUNDMODEL);

        return newGame.getGameModel(request.userId()).getRound();
    }

    @PostMapping("/fold")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public RoundModel foldGame(@RequestBody GameActionRequest request) {
        ch.uzh.ifi.hase.soprafs24.model.Game game = activeGames.get(request.sessionId());
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        Round round = game.getRound();
        round.handleFold(request.userId());

        wsHandler.sendModelToAll(Long.toString(game.getSessionId()), game, Model.ROUNDMODEL);
        if(round.isRoundOver())wsHandler.sendModelToAll(Long.toString(game.getSessionId()), game, Model.WINNINGMODEL);
        return game.getRoundModel(request.userId());
    }

    @PostMapping("/roundComplete")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ch.uzh.ifi.hase.soprafs24.model.Game completeRound(@RequestBody GameActionRequest request) {
        ch.uzh.ifi.hase.soprafs24.model.Game game = activeGames.get(request.sessionId());
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        Game gameEntity = gameService.completeRound(game);

        wsHandler.sendModelToAll(Long.toString(game.getSessionId()), game, Model.GAMEMODEL);

        return new ch.uzh.ifi.hase.soprafs24.model.Game(gameEntity, false);
    }

    @PostMapping("/call")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public RoundModel callGame(@RequestBody GameActionRequest request) {
        ch.uzh.ifi.hase.soprafs24.model.Game game = activeGames.get(request.sessionId());
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        Round round = game.getRound();
        round.handleCall(request.userId(), request.amount());

        wsHandler.sendModelToAll(Long.toString(game.getSessionId()), game, Model.ROUNDMODEL);
        if(round.isRoundOver())wsHandler.sendModelToAll(Long.toString(game.getSessionId()), game, Model.WINNINGMODEL);
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
        Round round = game.getRound();
        round.handleRaise(request.userId(), request.amount());

        wsHandler.sendModelToAll(Long.toString(game.getSessionId()), game, Model.ROUNDMODEL);
        if(round.isRoundOver())wsHandler.sendModelToAll(Long.toString(game.getSessionId()), game, Model.WINNINGMODEL);
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

    @PostMapping("/quit")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public boolean quitGame(@RequestBody GameActionRequest request) {
        Game game = gameService.getGameBySessionId(request.sessionId());
        gameService.removePlayerFromGame(game, request.userId());
        Game updatedGame = gameService.getGameBySessionId(request.sessionId());

        ch.uzh.ifi.hase.soprafs24.model.Game gameModel = new ch.uzh.ifi.hase.soprafs24.model.Game(updatedGame, false);
        wsHandler.sendModelToAll(Long.toString(gameModel.getSessionId()), gameModel, Model.GAMEMODEL);

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

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public boolean deleteGame(@RequestParam long sessionId, @RequestParam long userId) {
        Game game = gameService.getGameBySessionId(sessionId);
        if (game.getOwner().getId() != userId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this game");
        }
        if (game.isRoundRunning())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Game is still running");

        activeGames.remove(sessionId);
        gameService.deleteSession(game);
        return true;
    }

    @GetMapping("/allGames")
    @ResponseStatus(HttpStatus.OK)
    public List<Game> getAllGames() {
        return gameService.getAllGames();
    }

    @GetMapping("/owned/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public List<Game> getAllOwnedGames(@PathVariable long userId) {
        return gameService.getGamesOwnedByUser(userId);
    }

    @GetMapping("/settings/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    public ch.uzh.ifi.hase.soprafs24.model.GameSettings getGameSettings(@PathVariable long gameId) {
        gameService.getGameBySessionId(gameId);
        return gameSettingsService.getGameSettings(gameId).toModel();
    }

    @PostMapping("/settings")
    @ResponseStatus(HttpStatus.OK)
    public ch.uzh.ifi.hase.soprafs24.model.GameSettings getGameSettings(@RequestBody ModifyGameSettingsRequest request) {
        Game game = gameService.getGameBySessionId(request.sessionId());
        ch.uzh.ifi.hase.soprafs24.entity.GameSettings savedSettings = gameSettingsService.updateSettings(game.getSettings(), request.gameSettings());
        // TODO: push to all clients which are in the game
        return savedSettings.toModel();
    }

    @PostMapping("/check")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public RoundModel checkGame(@RequestBody GameActionRequest request) {
        ch.uzh.ifi.hase.soprafs24.model.Game game = activeGames.get(request.sessionId());
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        game.getRound().handleCheck(request.userId());

        wsHandler.sendModelToAll(Long.toString(game.getSessionId()), game, Model.ROUNDMODEL);

        return game.getRoundModel(request.userId());
    }
}
