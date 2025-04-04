package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    // Get a player
    public Player getPlayer(long userId) {
        Optional<Player> p = playerRepository.findByUserId(userId);
        if (p.isEmpty()) {
            throw new IllegalArgumentException("Player with id " + userId + " not found");
        }
        return p.get();
    }

    public void savePlayer(Player player) {
        playerRepository.save(player);
        playerRepository.flush();
    }
    // create a player
    public Player createPlayer(long userId, String name, long balance, Game game) {
        Player p = new Player(userId, name, balance, game);
        p.setIsOnline(true);
        p.setBalance(balance);
        playerRepository.save(p);
        playerRepository.flush();
        return p;
    }
    public List<Player> findByGameId(long gameId) {
        return playerRepository.findByGameId(gameId);
    }

    public void removePlayer(long userId, Game game) {
        Optional<Player> p = playerRepository.findByUserId(userId);
        if (p.isEmpty()) {
            throw new IllegalArgumentException("Player with id " + userId + " not found");
        }
        Player player = p.get();
        playerRepository.delete(player);
        playerRepository.flush();
    }

    public void deletePlayers(List<Player> players) {
        playerRepository.deleteAll(players);
        playerRepository.flush();
    }
}
