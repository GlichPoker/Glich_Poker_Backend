package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.AllowedUser;
import ch.uzh.ifi.hase.soprafs24.entity.AllowedUserId;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.AllowedUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AllowedUserService {

    private final AllowedUserRepository allowedUserRepository;

    @Autowired
    public AllowedUserService(AllowedUserRepository allowedUserRepository) {
        this.allowedUserRepository = allowedUserRepository;
    }

    public void addAllowedUser(Game game, User user) {
        if (!isUserAllowed(game.getSessionId(), user.getId())) {
            AllowedUser allowedUser = new AllowedUser(game, user);
            allowedUserRepository.save(allowedUser);
            allowedUserRepository.flush();
        }
    }

    public boolean isUserAllowed(Long gameId, Long userId) {
        Optional<AllowedUser> allowedUser = allowedUserRepository.findById_GameIdAndId_UserId(gameId, userId);
        return allowedUser.isPresent();
    }

    public List<User> getAllowedUsers(Long gameId) {
        List<AllowedUser> allowedUsers = allowedUserRepository.findById_GameId(gameId);
        return allowedUsers.stream()
                .map(AllowedUser::getUser)
                .toList();
    }

    public void removeAllowedUser(Long gameId, Long userId) {
        Optional<AllowedUser> allowedUser = allowedUserRepository.findById_GameIdAndId_UserId(gameId, userId);
        allowedUser.ifPresent(allowedUserRepository::delete);
        allowedUserRepository.flush();
    }
}