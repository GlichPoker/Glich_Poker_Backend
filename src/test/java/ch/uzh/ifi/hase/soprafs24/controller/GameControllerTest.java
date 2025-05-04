package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.HandRank;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.*;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.GameSettingsService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
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
public class GameControllerTest {

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

    private GameActionRequest gameActionRequest;
    private CreateGameRequest createGameRequest;
    private DenyInvitationRequest denyInvitationRequest;

    private User testUser;
    private User testUser2;
    private ch.uzh.ifi.hase.soprafs24.entity.GameSettings gameSettingsEntity;
    private GameSettings gameSettingsModel;
    private Game testGame;

    @BeforeEach
    public void setup() {
        // Initialize test data
        List<HandRank> order = new ArrayList<>(Arrays.stream(HandRank.values()).sorted(Comparator.reverseOrder()).toList());
        gameSettingsModel = new GameSettings(341, 2, 3, order, true);
        createGameRequest = new CreateGameRequest(1L, gameSettingsModel, true);
        gameActionRequest = new GameActionRequest(1L, 1L, 100);
        denyInvitationRequest = new DenyInvitationRequest(1L, 3, 3);

        // Configure ObjectMapper
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        // Create test users
        testUser = createUser(1L, "testUsername", "securePassword", "1", UserStatus.ONLINE);
        testUser2 = createUser(2L, "testUsername2", "securePassword2", "2", UserStatus.ONLINE);

        // Create game settings
        gameSettingsEntity = new ch.uzh.ifi.hase.soprafs24.entity.GameSettings(33443, 12, 23, order, true);

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
    public void testCreateGame() throws Exception {
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
    public void testCreateGameUserNotFound() throws Exception {
        when(userService.getUserById(anyLong())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(MockMvcRequestBuilders.post("/game/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGameRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateGameSettingsNotFound() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(testUser);
        when(gameSettingsService.createGameSettings(any())).thenReturn(gameSettingsEntity);
        when(gameService.createGame(any(), anyLong(), anyBoolean())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(MockMvcRequestBuilders.post("/game/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGameRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testInvitePlayer() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(testUser);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        mockMvc.perform(MockMvcRequestBuilders.post("/game/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isCreated())
                .andExpect(result -> assertTrue(Boolean.parseBoolean(result.getResponse().getContentAsString())));
    }

    @Test
    public void testInvitePlayer_GameNotFound() throws Exception {
        when(gameService.getGameBySessionId(anyLong())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(MockMvcRequestBuilders.post("/game/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDenyInvitation() throws Exception {
        addPlayersToGame(testGame, testUser);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        mockMvc.perform(MockMvcRequestBuilders.post("/game/denyInvitation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(denyInvitationRequest)))
                .andExpect(status().isOk())
                .andExpect(result -> assertTrue(Boolean.parseBoolean(result.getResponse().getContentAsString())));

        verify(gameService, times(1)).removePlayerFromGame(eq(testGame), eq(denyInvitationRequest.userId()));
    }

    @Test
    public void testDenyInvitationGame() throws Exception {
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);
        when(gameService.removePlayerFromGame(any(), anyLong())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(MockMvcRequestBuilders.post("/game/denyInvitation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(denyInvitationRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testJoinGame() throws Exception {
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);
        when(userService.getUserById(anyLong())).thenReturn(testUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/game/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(testGame.getSessionId()));

        verify(gameService, times(1)).handlePlayerJoinOrRejoin(eq(testGame), eq(testUser));
    }

    @Test
    public void testJoinGameUserAlreadyInGame() throws Exception {
        List<HandRank> order = new ArrayList<>(Arrays.stream(HandRank.values()).sorted(Comparator.reverseOrder()).toList());

        ch.uzh.ifi.hase.soprafs24.entity.GameSettings gameSettings = new ch.uzh.ifi.hase.soprafs24.entity.GameSettings(33443, 12, 23, order, true);
        User user = new User();
        user.setId(1L);
        user.setPassword("securePassword");
        user.setUsername("testUsername");
        user.setToken("1");
        user.setStatus(UserStatus.ONLINE);
        Game game = new Game(user, gameSettings, true);
        when(gameService.getGameBySessionId(gameActionRequest.sessionId())).thenReturn(game);
        when(userService.getUserById(gameActionRequest.userId())).thenReturn(user);
        when(gameService.handlePlayerJoinOrRejoin(game, user)).thenThrow(new ResponseStatusException(HttpStatus.CONFLICT));


        mockMvc.perform(MockMvcRequestBuilders.post("/game/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testStartGame() throws Exception {
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
    public void testStartGameNotEnoughUsersOnline() throws Exception {
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
    public void testFoldGame() throws Exception {
        addPlayersToGame(testGame, testUser, testUser2);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        ch.uzh.ifi.hase.soprafs24.model.Game gameModel = new ch.uzh.ifi.hase.soprafs24.model.Game(testGame, true);
        RoundModel round = gameModel.getRoundModel(testUser.getId());

        // First start the game
        mockMvc.perform(MockMvcRequestBuilders.post("/game/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk());

        // Then test fold
        mockMvc.perform(MockMvcRequestBuilders.post("/game/fold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.player.userId").value(round.getPlayer().getUserId()))
                .andExpect(jsonPath("$.otherPlayers[0].userId").value(testUser2.getId()));
    }

    @Test
    public void testFoldGameGameNotStarted() throws Exception {
        gameActionRequest = new GameActionRequest(12341,13,2134);
        mockMvc.perform(MockMvcRequestBuilders.post("/game/fold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCompleteRound() throws Exception {
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
    public void testCallGame() throws Exception {
        addPlayersToGame(testGame, testUser, testUser2);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        ch.uzh.ifi.hase.soprafs24.model.Game gameModel = new ch.uzh.ifi.hase.soprafs24.model.Game(testGame, false);

        // First start the game
        mockMvc.perform(MockMvcRequestBuilders.post("/game/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk());

        // Then test call
        mockMvc.perform(MockMvcRequestBuilders.post("/game/call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.player.userId").value(gameModel.getPlayer(testUser.getId()).getUserId()))
                .andExpect(jsonPath("$.player.roundBet").value(gameActionRequest.amount() + gameSettingsEntity.getSmallBlind()));
    }

    @Test
    public void testRaiseGame() throws Exception {
        addPlayersToGame(testGame, testUser, testUser2);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        ch.uzh.ifi.hase.soprafs24.model.Game gameModel = new ch.uzh.ifi.hase.soprafs24.model.Game(testGame, false);

        // First start the game
        mockMvc.perform(MockMvcRequestBuilders.post("/game/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk());

        // Then test raise
        mockMvc.perform(MockMvcRequestBuilders.post("/game/raise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.player.userId").value(gameModel.getPlayer(testUser.getId()).getUserId()))
                .andExpect(jsonPath("$.player.roundBet").value(gameActionRequest.amount() + gameSettingsEntity.getSmallBlind()));
    }

    @Test
    public void testLeaveGame() throws Exception {
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        mockMvc.perform(MockMvcRequestBuilders.post("/game/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk())
                .andExpect(result -> assertTrue(Boolean.parseBoolean(result.getResponse().getContentAsString())));
    }

    @Test
    public void testSaveGame() throws Exception {
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        mockMvc.perform(MockMvcRequestBuilders.post("/game/save")
                        .param("sessionId", String.valueOf(gameActionRequest.sessionId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> assertTrue(Boolean.parseBoolean(result.getResponse().getContentAsString())));
    }

    @Test
    public void testDeleteGame() throws Exception {
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        mockMvc.perform(MockMvcRequestBuilders.post("/game/delete")
                        .param("sessionId", String.valueOf(gameActionRequest.sessionId()))
                        .param("userId", String.valueOf(gameActionRequest.userId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> assertTrue(Boolean.parseBoolean(result.getResponse().getContentAsString())));
    }

    @Test
    public void testDeleteGameNotOwner() throws Exception {
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        mockMvc.perform(MockMvcRequestBuilders.post("/game/delete")
                        .param("sessionId", String.valueOf(gameActionRequest.sessionId()))
                        .param("userId", String.valueOf(2)) // Different user ID
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteGameRunning() throws Exception {
        testGame.setRoundRunning(true);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        mockMvc.perform(MockMvcRequestBuilders.post("/game/delete")
                        .param("sessionId", String.valueOf(gameActionRequest.sessionId()))
                        .param("userId", String.valueOf(gameActionRequest.userId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetAllGames() throws Exception {
        List<Game> games = Collections.singletonList(testGame);
        when(gameService.getAllGames()).thenReturn(games);

        mockMvc.perform(MockMvcRequestBuilders.get("/game/allGames")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    public void testGetAllOwned() throws Exception {
        List<Game> games = Collections.singletonList(testGame);
        when(gameService.getGamesOwnedByUser(anyLong())).thenReturn(games);

        mockMvc.perform(MockMvcRequestBuilders.get("/game/owned/{userId}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    public void testGetAllOwnedNoGames() throws Exception {
        when(gameService.getGamesOwnedByUser(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/game/owned/{userId}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testGetAllNoGames() throws Exception {
        when(gameService.getAllGames()).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/game/allGames")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testRejoinGame() throws Exception {
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);
        when(userService.getUserById(anyLong())).thenReturn(testUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/game/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(testGame.getSessionId()));

        verify(gameService, times(1)).handlePlayerJoinOrRejoin(eq(testGame), eq(testUser));
    }

    @Test
    public void testQuitGame() throws Exception {
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);
        mockMvc.perform(MockMvcRequestBuilders.post("/game/quit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk());
        verify(gameService, times(1)).removePlayerFromGame(eq(testGame), eq(gameActionRequest.userId()));
    }

    @Test
    public void testCheckGameConflict() throws Exception {
        addPlayersToGame(testGame, testUser, testUser2);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);

        ch.uzh.ifi.hase.soprafs24.model.Game gameModel = new ch.uzh.ifi.hase.soprafs24.model.Game(testGame, false);

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
    public void testCheckGame() throws Exception {
        addPlayersToGame(testGame, testUser, testUser2);
        when(gameService.getGameBySessionId(anyLong())).thenReturn(testGame);
        testGame.getSettings().setBigBlind(0);
        testGame.getSettings().setSmallBlind(0);
        ch.uzh.ifi.hase.soprafs24.model.Game gameModel = new ch.uzh.ifi.hase.soprafs24.model.Game(testGame, false);

        // First start the game
        mockMvc.perform(MockMvcRequestBuilders.post("/game/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk());

        // Then test call
        mockMvc.perform(MockMvcRequestBuilders.post("/game/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameActionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.player.userId").value(gameModel.getPlayer(testUser.getId()).getUserId()))
                .andExpect(jsonPath("$.player.roundBet").value(0));

    }

    @Test
    public void testDefaultOrder() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/game/defaultOrder")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0]").value(HandRank.ROYALFLUSH.toString())).andReturn()
        ;
    }
}