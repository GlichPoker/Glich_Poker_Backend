package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class InviteId implements Serializable {

    @Column(name = "game_id")
    private Long gameId;

    @Column(name = "user_id")
    private Long userId;

    public InviteId() {}

    public InviteId(Long gameId, Long userId) {
        this.gameId = gameId;
        this.userId = userId;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InviteId that = (InviteId) o;
        return Objects.equals(gameId, that.gameId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameId, userId);
    }
}