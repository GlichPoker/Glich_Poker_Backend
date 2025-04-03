package ch.uzh.ifi.hase.soprafs24.entity;
import ch.uzh.ifi.hase.soprafs24.constant.FriendRequestState;
import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "FRIENDS")
public class Friends implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Long user1Id;

    @Column(nullable = false)
    private Long user2Id;

    @Column(nullable = false)
    private FriendRequestState status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(Long id) {
        this.user1Id = id;
    }
    public Long getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(Long id) {
        this.user2Id = id;
    }

    public FriendRequestState getRequestStatus() {
        return status;
    }

    public void setRequestStatus(FriendRequestState status) {
        this.status = status;
    }
}