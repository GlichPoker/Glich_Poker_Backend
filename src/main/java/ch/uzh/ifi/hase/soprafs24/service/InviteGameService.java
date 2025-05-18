package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Invite;
import ch.uzh.ifi.hase.soprafs24.entity.AllowedUserId;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.GameInvitesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class InviteGameService {

    private final GameInvitesRepository gameinvitesRepository;

    @Autowired
    public InviteGameService(GameInvitesRepository gameinvitesRepository) {
        this.gameinvitesRepository = gameinvitesRepository;
    }

    public void addAllowedUser(Game game, User user) {
        if (!isUserAllowed(game.getSessionId(), user.getId())) {
            Invite allowedUser = new Invite(game, user);
            gameinvitesRepository.save(allowedUser);
            gameinvitesRepository.flush();
        }
    }

    public boolean isUserAllowed(Long gameId, Long userId) {
        Optional<Invite> allowedUser = gameinvitesRepository.findById_GameIdAndId_UserId(gameId, userId);
        return allowedUser.isPresent();
    }

    public List<User> getAllowedUsers(Long gameId) {
        List<Invite> allowedUsers = gameinvitesRepository.findById_GameId(gameId);
        return allowedUsers.stream()
                .map(Invite::getUser)
                .toList();
    }

    public void removeAllowedUser(Long gameId, Long userId) {
        Optional<Invite> allowedUser = gameinvitesRepository.findById_GameIdAndId_UserId(gameId, userId);
        allowedUser.ifPresent(gameinvitesRepository::delete);
        gameinvitesRepository.flush();
    }
}