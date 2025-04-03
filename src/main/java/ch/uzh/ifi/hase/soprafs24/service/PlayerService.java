package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Player savePlayer(Player player) {
        player = playerRepository.save(player);
        playerRepository.flush();
        return player;

    }
    // create a player
    public Player createPlayer(long userId, String name, long balance) {
        Player p = new Player(userId, name, balance);
        p.setIsOnline(false);
        p.setBalance(balance);
        playerRepository.save(p);
        playerRepository.flush();
        return p;
    }
}
