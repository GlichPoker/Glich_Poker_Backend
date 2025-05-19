package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.HandRank;
import ch.uzh.ifi.hase.soprafs24.constant.Model;
import ch.uzh.ifi.hase.soprafs24.constant.WeatherType;
import ch.uzh.ifi.hase.soprafs24.constant.UserLobbyStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.*;
import ch.uzh.ifi.hase.soprafs24.service.*;
import ch.uzh.ifi.hase.soprafs24.websockets.WS_Handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/game")
public class GameController {

    private final InviteGameService allowedUserService;

    private final ConcurrentHashMap<Long, ch.uzh.ifi.hase.soprafs24.model.Game> activeGames = new ConcurrentHashMap<>();
    private final GameService gameService;
    private final GameSettingsService gameSettingsService;
    private final UserService userService;
    private final WS_Handler wsHandler;
    private final ModelPusher modelPusher;
    private final PlayerStatisticsService playerStatisticsService;

    @Autowired
    public GameController(GameService gameService, GameSettingsService gameSettingsService, UserService userService,
            WS_Handler wsHandler, ModelPusher modelPusher, PlayerStatisticsService playerStatisticsService, InviteGameService allowedUserService) {
        this.gameService = gameService;
        this.gameSettingsService = gameSettingsService;
        this.userService = userService;
        this.wsHandler = wsHandler;
        this.modelPusher = modelPusher;
        this.playerStatisticsService = playerStatisticsService;
        this.allowedUserService = allowedUserService;
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
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
    public boolean invitePlayer(@RequestBody InvitationRequest request) {
        Game game = gameService.getGameBySessionId(request.sessionId());
        User user = userService.getUserById(request.userId());
        User sendingUser = userService.getUserById(request.senderId());
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        if (game.getOwner().getId() != sendingUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this game");
        }
        allowedUserService.addAllowedUser(game, user);
        //happens only after the invitation is accepted:
        //gameService.addPlayerToGame(game, user, game.getSettings().getInitialBalance());
        return true;
    }

    @PostMapping("/acceptInvitation")
    @ResponseStatus(HttpStatus.OK)
    public boolean acceptInvitation(@RequestBody InvitationRequest request) {
        Game game = gameService.getGameBySessionId(request.sessionId());
        User user = userService.getUserById(request.userId());
        allowedUserService.acceptInvite(game, user);
        System.out.println("Invitation accepted");

        return true;
    }

    @PostMapping("/declineInvitation")
    @ResponseStatus(HttpStatus.OK)
    public boolean declineInvitation(@RequestBody InvitationRequest request) {
        Game game = gameService.getGameBySessionId(request.sessionId());
        User user = userService.getUserById(request.userId());
        User sendingUser = userService.getUserById(request.senderId());

        if (!game.getOwner().getId().equals(sendingUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this game");
        }

        allowedUserService.rejectInvite(game, user);
        return true;
    }

    @PostMapping("/removePlayer")
    @ResponseStatus(HttpStatus.OK)
    public boolean removePlayer(@RequestBody GameActionRequest request) {
        Game game = gameService.getGameBySessionId(request.sessionId());
        User user = userService.getUserById(request.userId());
        gameService.removePlayerFromGame(game, user.getId());
        return true;
    }

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.OK)
    public ch.uzh.ifi.hase.soprafs24.model.Game joinGame(@RequestBody JoinGameRequest request) {
        Game game = gameService.getGameBySessionId(request.sessionId());
        User user = userService.getUserById(request.userId());
        gameService.handlePlayerJoinOrRejoin(game, user, request.password());
        user.setUserLobbyStatus(UserLobbyStatus.IN_LOBBY);
        user.setCurrentLobbyId(game.getSessionId());
        userService.saveUser(user);
        Game updatedGame = gameService.getGameBySessionId(request.sessionId());

        ch.uzh.ifi.hase.soprafs24.model.Game gameModel = new ch.uzh.ifi.hase.soprafs24.model.Game(updatedGame, false);
        wsHandler.sendModelToAll(Long.toString(gameModel.getSessionId()), gameModel, Model.GAMEMODEL);

        return gameModel;
    }

    @PostMapping("/swap")
    @ResponseStatus(HttpStatus.OK)
    public Card[] swapCard(@RequestBody SwapCardRequest request) {
        ch.uzh.ifi.hase.soprafs24.model.Game game = activeGames.get(request.sessionId());
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }

        if (game.getSettings().weatherType() != WeatherType.RAINY) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only allowed if rainy weather");
        }

        Round round = game.getRound();

        return round.updatePlayerHand(request.userId(), request.card());
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.OK)
    public RoundModel startGame(@RequestBody GameActionRequest request) {
        Game game = gameService.getGameBySessionId(request.sessionId());

        if (game.getRoundCount() % 3 == 0 && game.getRoundCount() > 0
                && game.getSettings().getWeatherType() == WeatherType.SUNNY) {
            ch.uzh.ifi.hase.soprafs24.entity.GameSettings settings = game.getSettings();
            long smallBlindIncreased = settings.getSmallBlind() > 19 ? (long) (settings.getSmallBlind() * 1.05)
                    : settings.getSmallBlind() + 1;
            long bigBlindIncreased = settings.getBigBlind() > 19 ? (long) (settings.getBigBlind() * 1.05)
                    : settings.getBigBlind() + 1;
            gameSettingsService.updateBlinds(settings, smallBlindIncreased, bigBlindIncreased);
            game = gameService.getGameBySessionId(request.sessionId());
        }

        ch.uzh.ifi.hase.soprafs24.model.Game newGame = new ch.uzh.ifi.hase.soprafs24.model.Game(game, true);
        gameService.startRound(game);

        for (Player player : newGame.getPlayers()) {
            player.setIsOnline(true);
        }

        activeGames.put(request.sessionId(), newGame);

        wsHandler.sendGameStateToAll(Long.toString(request.sessionId()), "IN_GAME");

        wsHandler.sendModelToAll(Long.toString(newGame.getSessionId()), newGame, Model.ROUNDMODEL);

        return newGame.getGameModel(request.userId()).getRound();
    }

    @PostMapping("/fold")
    @ResponseStatus(HttpStatus.OK)
    public void foldGame(@RequestBody GameActionRequest request) {
        ch.uzh.ifi.hase.soprafs24.model.Game game = activeGames.get(request.sessionId());
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        Round round = game.getRound();
        round.handleFold(request.userId());

        modelPusher.pushModel(round, game, wsHandler, gameService);
    }

    @PostMapping("/forceFold")
    @ResponseStatus(HttpStatus.OK)
    public void forceFoldGame(@RequestBody GameActionRequest request) {
        System.out.printf("[FORCE_FOLD] Request received from userId=%d, sessionId=%d%n", request.userId(),
                request.sessionId());
        ch.uzh.ifi.hase.soprafs24.model.Game game = activeGames.get(request.sessionId());
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        Round round = game.getRound();
        round.handleFold(request.userId());
        Game gameEntity = gameService.getGameBySessionId(request.sessionId());
        if (!gameEntity.isRoundRunning()) {
            gameService.setPlayerOffline(gameEntity, request.userId());
        }

        modelPusher.pushModel(round, game, wsHandler, gameService);
    }

    @PostMapping("/roundComplete")
    @ResponseStatus(HttpStatus.OK)
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
    public void callGame(@RequestBody GameActionRequest request) {
        ch.uzh.ifi.hase.soprafs24.model.Game game = activeGames.get(request.sessionId());
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        Round round = game.getRound();
        round.handleCall(request.userId(), request.amount());

        modelPusher.pushModel(round, game, wsHandler, gameService);
    }

    @PostMapping("/raise")
    @ResponseStatus(HttpStatus.OK)
    public void raiseGame(@RequestBody GameActionRequest request) {
        ch.uzh.ifi.hase.soprafs24.model.Game game = activeGames.get(request.sessionId());
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        Round round = game.getRound();
        round.handleRaise(request.userId(), request.amount());

        modelPusher.pushModel(round, game, wsHandler, gameService);

    }

    @PostMapping("/leave")
    @ResponseStatus(HttpStatus.OK)
    public boolean leaveGame(@RequestBody GameActionRequest request) {
        Game game = gameService.getGameBySessionId(request.sessionId());
        gameService.setPlayerOffline(game, request.userId());
        return true;
    }

    @PostMapping("/quit")
    @ResponseStatus(HttpStatus.OK)
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
    public boolean saveGame(@RequestParam long sessionId) {
        Game game = gameService.getGameBySessionId(sessionId);
        activeGames.remove(sessionId);
        gameService.saveSession(game);
        return true;
    }

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public boolean deleteGame(@RequestParam long sessionId, @RequestParam long userId) {
        Game game = gameService.getGameBySessionId(sessionId);
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        if (game.getOwner().getId() != userId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this game");
        }
        if (game.isRoundRunning())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Game is still running");
        // add the statistics to the user
        for (ch.uzh.ifi.hase.soprafs24.entity.Player player : game.getAllPlayers()) {
            User user = player.getUser();
            playerStatisticsService.incrementUser_games_played(user);
            playerStatisticsService.updateUser_round_played(user, game.getRoundCount());
            playerStatisticsService.updateUser_BB_100_record(user, game, player);
        }

        activeGames.remove(sessionId);
        gameService.deleteSession(game);
        return true;
    }

    @GetMapping("/allGames")
    @ResponseStatus(HttpStatus.OK)
    public List<GameModel> getAllGames() {
        List<Game> games = gameService.getAllGames();
        List<GameModel> gameModels = new ArrayList<>();
        for (Game game : games) {
            gameModels.add(new GameModel(game.toGameModel(), -1));
        }
        return gameModels;
    }

    @GetMapping("/owned/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public List<GameModel> getAllOwnedGames(@PathVariable long userId) {
        List<Game> games = gameService.getGamesOwnedByUser(userId);
        List<GameModel> gameModels = new ArrayList<>();
        for (Game game : games) {
            gameModels.add(new GameModel(game.toGameModel(), -1));
        }
        return gameModels;
    }

    @GetMapping("/settings/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    public ch.uzh.ifi.hase.soprafs24.model.GameSettings getGameSettings(@PathVariable long gameId) {
        gameService.getGameBySessionId(gameId);
        return gameSettingsService.getGameSettings(gameId).toModel();
    }

    @PostMapping("/settings")
    @ResponseStatus(HttpStatus.OK)
    public ch.uzh.ifi.hase.soprafs24.model.GameSettings getGameSettings(
            @RequestBody ModifyGameSettingsRequest request) {
        Game game = gameService.getGameBySessionId(request.sessionId());
        ch.uzh.ifi.hase.soprafs24.entity.GameSettings savedSettings = gameSettingsService
                .updateSettings(game.getSettings(), request.gameSettings());

        Map<String, Object> weatherUpdateMessage = new HashMap<>();
        weatherUpdateMessage.put("event", "WEATHER_UPDATED");
        weatherUpdateMessage.put("weatherType", savedSettings.getWeatherType().toString());

        wsHandler.sendGenericToAll(Long.toString(game.getSessionId()), weatherUpdateMessage);

        return savedSettings.toModel();
    }

    @PostMapping("/check")
    @ResponseStatus(HttpStatus.OK)
    public void checkGame(@RequestBody GameActionRequest request) {
        ch.uzh.ifi.hase.soprafs24.model.Game game = activeGames.get(request.sessionId());
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        Round round = game.getRound();
        round.handleCheck(request.userId());

        modelPusher.pushModel(round, game, wsHandler, gameService);
    }

    @PostMapping("/readyForNextGame")
    @ResponseStatus(HttpStatus.OK)
    public ch.uzh.ifi.hase.soprafs24.model.Game readyForNextGame(@RequestBody GameActionRequest request) {
        ch.uzh.ifi.hase.soprafs24.model.Game game = activeGames.get(request.sessionId());
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }

        game.roundComplete();
        wsHandler.sendGameStateToAll(Long.toString(game.getSessionId()), "PRE_GAME");
        wsHandler.sendModelToAll(Long.toString(game.getSessionId()), game, Model.GAMEMODEL);

        return game;
    }

    @GetMapping("/defaultOrder")
    @ResponseStatus(HttpStatus.OK)
    public List<HandRank> getDefaultOrder() {
        return new ArrayList<>(Arrays.stream(HandRank.values()).sorted(Comparator.reverseOrder()).toList());
    }

    @GetMapping("/bluffCards")
    @ResponseStatus(HttpStatus.OK)
    public List<Card> getBluffCards(@RequestParam long playerId, @RequestParam long sessionId) {
        ch.uzh.ifi.hase.soprafs24.model.Game game = activeGames.get(sessionId);
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }

        Player player = game.getPlayer(playerId);
        if (player == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found");
        }
        return new Deck().getCards();
    }

    @PostMapping("/bluff")
    @ResponseStatus(HttpStatus.OK)
    public void bluff(@RequestBody SwapCardRequest request) {
        ch.uzh.ifi.hase.soprafs24.model.Game game = activeGames.get(request.sessionId());
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }

        if (game.getSettings().weatherType() != WeatherType.SUNNY) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Can only bluff if its sunny weather");
        }
        Player player = game.getPlayer(request.userId());
        if (player == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found");
        }
        BluffModel model = new BluffModel(request.userId(), request.card());
        wsHandler.sendBluffModelToAll(Long.toString(game.getSessionId()), model);
    }

    @GetMapping("/stats/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> getPlayerStatistics(@PathVariable long userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        Map<String, Object> stats = new HashMap<>();
        stats.put("gamesPlayed", playerStatisticsService.getPlayer_games_played(user));
        stats.put("roundsPlayed", playerStatisticsService.getPlayer_round_played(user));
        stats.put("bb100", playerStatisticsService.getPlayer_BB_100(user));
        double bb_won = playerStatisticsService.getPlayer_BB_100(user)
                * (playerStatisticsService.getPlayer_BB_100_count(user) / 100.0);
        stats.put("bbWon", bb_won);
        stats.put("bankrupts", playerStatisticsService.getPlayer_bankrupt(user));
        return stats;
    }
}
