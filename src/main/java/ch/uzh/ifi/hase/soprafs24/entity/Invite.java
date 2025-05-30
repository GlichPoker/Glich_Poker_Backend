package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "allowed_user") // Defines the table name
public class Invite {

    @EmbeddedId
    private InviteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("gameId") // Maps the gameId field of the embedded ID
    @JoinColumn(name = "game_id", referencedColumnName = "id")
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId") // Maps the userId field of the embedded ID
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public Invite() {
    }

    public Invite(Game game, User user) {
        this.game = game;
        this.user = user;
        this.id = new InviteId(game.getSessionId(), user.getId());
    }

    // Standard getters and setters
    public InviteId getId() {
        return id;
    }

    public void setId(InviteId id) {
        this.id = id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invite that = (Invite) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
