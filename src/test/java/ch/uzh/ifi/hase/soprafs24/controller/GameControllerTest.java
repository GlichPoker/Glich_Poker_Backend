package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.*;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.*;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.GameSettingsService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.websockets.WS_Handler;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GameService gameService;

    @MockBean
    private UserService userService;

    @MockBean
    private GameSettingsService gameSettingsService;

    @MockBean
    private ModelPusher modelPusher;

    @MockBean
    private WS_Handler wsHandler;

    private GameActionRequest gameActionRequest;
    private JoinGameRequest joinGameRequest;
    private CreateGameRequest createGameRequest;
    private DenyInvitationRequest denyInvitationRequest;

    private User testUser;
    private User testUser2;
    private ch.uzh.ifi.hase.soprafs24.entity.GameSettings gameSettingsEntity;
    private GameSettings gameSettingsModel;
    private Game testGame;
    private List<HandRank> order;

    @BeforeEach
    void setup() {
        // Initialize test data
        order = new ArrayList<>(Arrays.stream(HandRank.values()).sorted(Comparator.reverseOrder()).toList());
        gameSettingsModel = new GameSettings(341, 2, 3, order, true, WeatherType.CLOUDY, "");
        createGameRequest = new CreateGameRequest(1L, gameSettingsModel, true);
        gameActionRequest = new GameActionRequest(1L, 1L, 100);
        joinGameRequest = new JoinGameRequest(1L, 1L, "");
        denyInvitationRequest = new DenyInvitationRequest(1L, 3, 3);

        // Configure ObjectMapper
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        // Create test users
        testUser = createUser(1L, "testUsername", "securePassword", "1", UserStatus.ONLINE);
        testUser2 = createUser(2L, "testUsername2", "securePassword2", "2", UserStatus.ONLINE);

        // Create game settings
        gameSettingsEntity = new ch.uzh.ifi.hase.soprafs24.entity.GameSettings(33443, 12, 23, order, true, WeatherType.CLOUDY, "");

        // Create test game
        testGame = new Game(testUser, gameSettingsEntity, true);
    }

    private User createUser(Long id, String username, String password, String token, UserStatus status) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setToken(token);
        user.setStatus(status);
        return user;
    }

    private void addPlayersToGame(Game game, User... users) {
        for (User user : users) {
            ch.uzh.ifi.hase.soprafs24.entity.Player player =
                    new ch.uzh.ifi.hase.soprafs24.entity.Player(user, game.getSettings().getInitialBalance(), game);
            player.setIsOnline(true);
            game.addPlayer(player);
        }
    }

    @Test
    void testCreateGameUserNotFound() throws Exception {
        when(userService.getUserById(anyLong())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(MockMvcRequestBuilders.post("/game/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGameRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateGame() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(testUser);
        when(gameSettingsService.createGameSettings(any())).thenReturn(gameSettingsEntity);
        when(gameService.createGame(any(), anyLong(), anyBoolean())).thenReturn(testGame);

        mockMvc.perform(MockMvcRequestBuilders.post("/game/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGameRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").value(testGame.getSessionId()))
                .andExpect(jsonPath("$.ownerId").value(testGame.getOwner().getId()))
                .andExpect(jsonPath("$.settings.initialBalance").value(gameSettingsEntity.getInitialBalance()))
                .andExpect(jsonPath("$.round", is(nullValue())))
                .andExpect(jsonPath("$.players", hasSize(0)))
                .andExpect(jsonPath("$.currentRoundStartPlayer").value(testGame.getStartPlayer()));
    }

    @Test
    void testCreateGameSettingsNotFound() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(testUser);
        when(gameSettingsService.createGameSettings(any())).thenReturn(gameSettingsEntity);
        when(gameService.createGame(any(), anyLong(), anyBoolean())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(MockMvcRequestBuilders.post("/game/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGameRequest)))
                .andExpect(status().isNotFound());
    }
/*
    @Test
    void testInvitePlayer() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(testUser);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        mockMvc.perform(MockMvcRequestBuilders.post("/game/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isCreated())
                .andExpect(result -> assertTrue(Boolean.parseBoolean(result.getResponse().getContentAsString())));
    }
*/
    @Test
    void testInvitePlayer_GameNotFound() throws Exception {
        when(gameService.getGameBySessionId(anyLong())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(MockMvcRequestBuilders.post("/game/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isNotFound());
    }
/*
    @Test
    void testDenyInvitation() throws Exception {
        addPlayersToGame(testGame, testUser);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        mockMvc.perform(MockMvcRequestBuilders.post("/game/denyInvitation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(denyInvitationRequest)))
                .andExpect(status().isOk())
                .andExpect(result -> assertTrue(Boolean.parseBoolean(result.getResponse().getContentAsString())));

        verify(gameService, times(1)).removePlayerFromGame(testGame, denyInvitationRequest.userId());
    }
*/
    @Test
    void testDenyInvitationGame() throws Exception {
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);
        when(gameService.removePlayerFromGame(any(), anyLong())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(MockMvcRequestBuilders.post("/game/denyInvitation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(denyInvitationRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testJoinGame() throws Exception {
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);
        when(userService.getUserById(anyLong())).thenReturn(testUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/game/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinGameRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(testGame.getSessionId()));

        verify(gameService, times(1)).handlePlayerJoinOrRejoin(testGame, testUser, "");
    }

    @Test
    void testJoinGameUserAlreadyInGame() throws Exception {
        List<HandRank> revorder = new ArrayList<>(Arrays.stream(HandRank.values()).sorted(Comparator.reverseOrder()).toList());

        ch.uzh.ifi.hase.soprafs24.entity.GameSettings gameSettings = new ch.uzh.ifi.hase.soprafs24.entity.GameSettings(33443, 12, 23, revorder, true, WeatherType.CLOUDY, "");
        User user = new User();
        user.setId(1L);
        user.setPassword("securePassword");
        user.setUsername("testUsername");
        user.setToken("1");
        user.setStatus(UserStatus.ONLINE);
        Game game = new Game(user, gameSettings, true);
        when(gameService.getGameBySessionId(gameActionRequest.sessionId())).thenReturn(game);
        when(userService.getUserById(gameActionRequest.userId())).thenReturn(user);
        when(gameService.handlePlayerJoinOrRejoin(game, user, "")).thenThrow(new ResponseStatusException(HttpStatus.CONFLICT));


        mockMvc.perform(MockMvcRequestBuilders.post("/game/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinGameRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void testStartGame() throws Exception {
        addPlayersToGame(testGame, testUser, testUser2);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        ch.uzh.ifi.hase.soprafs24.model.Game gameModel = new ch.uzh.ifi.hase.soprafs24.model.Game(testGame, true);
        RoundModel round = gameModel.getRoundModel(testUser.getId());

        mockMvc.perform(MockMvcRequestBuilders.post("/game/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.potSize").value(round.getPotSize()))
                .andExpect(jsonPath("$.player.userId").value(round.getPlayer().getUserId()));
    }

    @Test
    void testStartGameBlindIncreaseApplied() throws Exception {
        addPlayersToGame(testGame, testUser, testUser2);
        testGame.setRoundCount(3);
        testGame.getSettings().setWeatherType(WeatherType.SUNNY);
        testGame.getSettings().setSmallBlind(103);
        testGame.getSettings().setBigBlind(206);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);
        when(gameSettingsService.updateBlinds(testGame.getSettings(), 103, 206)).thenReturn(testGame.getSettings());

        ch.uzh.ifi.hase.soprafs24.model.Game gameModel = new ch.uzh.ifi.hase.soprafs24.model.Game(testGame, true);
        RoundModel round = gameModel.getRoundModel(testUser.getId());

        mockMvc.perform(MockMvcRequestBuilders.post("/game/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.potSize").value(round.getPotSize()))
                .andExpect(jsonPath("$.player.userId").value(round.getPlayer().getUserId()))
                .andExpect((jsonPath("$.gameSettings.smallBlind").value(103)))
                .andExpect(jsonPath("$.gameSettings.bigBlind").value(206));
    }

    @Test
    void testStartGameNotEnoughUsersOnline() throws Exception {
        // Add only one online player
        ch.uzh.ifi.hase.soprafs24.entity.Player player =
                new ch.uzh.ifi.hase.soprafs24.entity.Player(testUser, gameSettingsEntity.getInitialBalance(), testGame);
        player.setIsOnline(true);
        testGame.addPlayer(player);

        // Add second player but offline
        ch.uzh.ifi.hase.soprafs24.entity.Player player2 =
                new ch.uzh.ifi.hase.soprafs24.entity.Player(testUser2, gameSettingsEntity.getInitialBalance(), testGame);
        player2.setIsOnline(false);
        testGame.addPlayer(player2);

        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        mockMvc.perform(MockMvcRequestBuilders.post("/game/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void testFoldGame() throws Exception {
        addPlayersToGame(testGame, testUser, testUser2);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        new ch.uzh.ifi.hase.soprafs24.model.Game(testGame, true);

        // First start the game
        mockMvc.perform(MockMvcRequestBuilders.post("/game/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk()).andReturn();

        // Then test fold
        mockMvc.perform(MockMvcRequestBuilders.post("/game/fold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk());
        verify(modelPusher, times(1)).pushModel(any(), any(), any(), any());

    }

    @Test
    void testForceFoldGame() throws Exception {
        addPlayersToGame(testGame, testUser, testUser2);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        new ch.uzh.ifi.hase.soprafs24.model.Game(testGame, true);

        // First start the game
        mockMvc.perform(MockMvcRequestBuilders.post("/game/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk()).andReturn();

        // Then test fold
        mockMvc.perform(MockMvcRequestBuilders.post("/game/forceFold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk());
        verify(modelPusher, times(1)).pushModel(any(), any(), any(), any());

    }

    @Test
    void testFoldGameGameNotStarted() throws Exception {
        gameActionRequest = new GameActionRequest(12341,13,2134);
        mockMvc.perform(MockMvcRequestBuilders.post("/game/fold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCompleteRound() throws Exception {
        addPlayersToGame(testGame, testUser, testUser2);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);
        when(gameService.completeRound(any())).thenReturn(testGame);

        // First start the game
        mockMvc.perform(MockMvcRequestBuilders.post("/game/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk());

        // Then complete the round
        mockMvc.perform(MockMvcRequestBuilders.post("/game/roundComplete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(testGame.getSessionId()))
                .andExpect(jsonPath("$.ownerId").value(testGame.getOwner().getId()));
    }

    @Test
    void testCallGame() throws Exception {
        addPlayersToGame(testGame, testUser, testUser2);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        // First start the game
        mockMvc.perform(MockMvcRequestBuilders.post("/game/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk());

        // Then test call
        mockMvc.perform(MockMvcRequestBuilders.post("/game/call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk());
        verify(modelPusher, times(1)).pushModel(any(), any(), any(), any());

    }

    @Test
    void testRaiseGame() throws Exception {
        addPlayersToGame(testGame, testUser, testUser2);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        // First start the game
        mockMvc.perform(MockMvcRequestBuilders.post("/game/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk());

        // Then test raise
        mockMvc.perform(MockMvcRequestBuilders.post("/game/raise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk());
        verify(modelPusher, times(1)).pushModel(any(), any(), any(), any());

    }

    @Test
    void testSwapCardNotRainy() throws Exception {
        addPlayersToGame(testGame, testUser, testUser2);
        testGame.getSettings().setWeatherType(WeatherType.SUNNY);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        // First start the game
        mockMvc.perform(MockMvcRequestBuilders.post("/game/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk()).andReturn();

        SwapCardRequest req = new SwapCardRequest(gameActionRequest.sessionId(), 1L, new Card(Rank.ACE, Suit.CLUBS));
        mockMvc.perform(MockMvcRequestBuilders.post("/game/swap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden()).andReturn();
    }

    @Test
    void testSwapCardNotCard() throws Exception {
        addPlayersToGame(testGame, testUser, testUser2);
        testGame.getSettings().setWeatherType(WeatherType.RAINY);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);
        // First start the game
        mockMvc.perform(MockMvcRequestBuilders.post("/game/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk()).andReturn();

        SwapCardRequest req = new SwapCardRequest(gameActionRequest.sessionId(), 1L, new Card(Rank.ACE, Suit.CLUBS));
        mockMvc.perform(MockMvcRequestBuilders.post("/game/swap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }


    @Test
    void testLeaveGame() throws Exception {
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        mockMvc.perform(MockMvcRequestBuilders.post("/game/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk())
                .andExpect(result -> assertTrue(Boolean.parseBoolean(result.getResponse().getContentAsString())));
    }

    @Test
    void testSaveGame() throws Exception {
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        mockMvc.perform(MockMvcRequestBuilders.post("/game/save")
                        .param("sessionId", String.valueOf(gameActionRequest.sessionId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> assertTrue(Boolean.parseBoolean(result.getResponse().getContentAsString())));
    }

    @Test
    void testDeleteGame() throws Exception {
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        mockMvc.perform(MockMvcRequestBuilders.post("/game/delete")
                        .param("sessionId", String.valueOf(gameActionRequest.sessionId()))
                        .param("userId", String.valueOf(gameActionRequest.userId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> assertTrue(Boolean.parseBoolean(result.getResponse().getContentAsString())));
    }

    @Test
    void testDeleteGameNotOwner() throws Exception {
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        mockMvc.perform(MockMvcRequestBuilders.post("/game/delete")
                        .param("sessionId", String.valueOf(gameActionRequest.sessionId()))
                        .param("userId", String.valueOf(2)) // Different user ID
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteGameRunning() throws Exception {
        testGame.setRoundRunning(true);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        mockMvc.perform(MockMvcRequestBuilders.post("/game/delete")
                        .param("sessionId", String.valueOf(gameActionRequest.sessionId()))
                        .param("userId", String.valueOf(gameActionRequest.userId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetAllGames() throws Exception {
        List<Game> games = Collections.singletonList(testGame);
        when(gameService.getAllGames()).thenReturn(games);

        mockMvc.perform(MockMvcRequestBuilders.get("/game/allGames")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testGetAllOwned() throws Exception {
        List<Game> games = Collections.singletonList(testGame);
        when(gameService.getGamesOwnedByUser(anyLong())).thenReturn(games);

        mockMvc.perform(MockMvcRequestBuilders.get("/game/owned/{userId}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testGetAllOwnedNoGames() throws Exception {
        when(gameService.getGamesOwnedByUser(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/game/owned/{userId}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetAllNoGames() throws Exception {
        when(gameService.getAllGames()).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/game/allGames")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testQuitGame() throws Exception {
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);
        mockMvc.perform(MockMvcRequestBuilders.post("/game/quit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk());
        verify(gameService, times(1)).removePlayerFromGame(testGame, gameActionRequest.userId());
    }

    @Test
    void testCheckGameConflict() throws Exception {
        addPlayersToGame(testGame, testUser, testUser2);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        // First start the game
        mockMvc.perform(MockMvcRequestBuilders.post("/game/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk());

        // Then test check
        mockMvc.perform(MockMvcRequestBuilders.post("/game/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void testCheckGame() throws Exception {
        addPlayersToGame(testGame, testUser, testUser2);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);
        testGame.getSettings().setBigBlind(0);
        testGame.getSettings().setSmallBlind(0);

        // First start the game
        mockMvc.perform(MockMvcRequestBuilders.post("/game/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk());

        // Then test call
        mockMvc.perform(MockMvcRequestBuilders.post("/game/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk());
        verify(modelPusher, times(1)).pushModel(any(), any(), any(), any());

    }

    @Test
    void testDefaultOrder() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/game/defaultOrder")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(HandRank.ROYALFLUSH.toString()));
    }

    @Test
    void testModifySettings() throws Exception {
        GameSettings s = new GameSettings(1000, 10, 20, order, true, WeatherType.RAINY, "hallo");
        ch.uzh.ifi.hase.soprafs24.entity.GameSettings entity = new ch.uzh.ifi.hase.soprafs24.entity.GameSettings(s.initialBalance(), s.smallBlind(), s.bigBlind(), s.order(), s.descending(), s.weatherType(), s.password());
        ModifyGameSettingsRequest request = new ModifyGameSettingsRequest(testGame.getSessionId(), s);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);
        when(gameSettingsService.updateSettings(gameSettingsEntity, s)).thenReturn(entity);

        mockMvc.perform(MockMvcRequestBuilders.post("/game/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bigBlind").value(s.bigBlind()))
                .andExpect(jsonPath("$.smallBlind").value(s.smallBlind()))
                .andExpect(jsonPath("$.initialBalance").value(s.initialBalance()))
                .andExpect(jsonPath("$.descending").value(s.descending()))
                .andExpect(jsonPath("$.weatherType").value(s.weatherType().toString()));
    }

    @Test
    void testReadyForNextGame() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/game/readyForNextGame")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testSettings() throws Exception {
        when(gameSettingsService.getGameSettings(anyLong())).thenReturn(gameSettingsEntity);

        mockMvc.perform(MockMvcRequestBuilders.get("/game/settings/{gameId}", testGame.getSessionId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testBluffCards() throws Exception {
        addPlayersToGame(testGame, testUser, testUser2);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        // First start the game
        mockMvc.perform(MockMvcRequestBuilders.post("/game/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk()).andReturn();

        mockMvc.perform(MockMvcRequestBuilders.get("/game/bluffCards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("sessionId", String.valueOf(gameActionRequest.sessionId()))
                        .param("playerId", String.valueOf(testUser.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(52));
    }

    @Test
    void testBluff() throws Exception {
        addPlayersToGame(testGame, testUser, testUser2);
        testGame.getSettings().setWeatherType(WeatherType.SUNNY);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        // First start the game
        mockMvc.perform(MockMvcRequestBuilders.post("/game/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk()).andReturn();

        SwapCardRequest req = new SwapCardRequest(gameActionRequest.sessionId(), testUser.getId(), new Card(Rank.ACE, Suit.CLUBS));
        mockMvc.perform(MockMvcRequestBuilders.post("/game/bluff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
        verify(wsHandler, times(1)).sendBluffModelToAll(any(), any());
    }

    @Test
    void testBluffNotSunny() throws Exception {
        addPlayersToGame(testGame, testUser, testUser2);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        // First start the game
        mockMvc.perform(MockMvcRequestBuilders.post("/game/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk()).andReturn();

        SwapCardRequest req = new SwapCardRequest(gameActionRequest.sessionId(), testUser.getId(), new Card(Rank.ACE, Suit.CLUBS));
        mockMvc.perform(MockMvcRequestBuilders.post("/game/bluff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}