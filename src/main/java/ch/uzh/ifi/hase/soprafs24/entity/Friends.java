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

    @ManyToOne
    @JoinColumn(name = "user1Id", referencedColumnName = "id")
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user2Id", referencedColumnName = "id")
    private User user2;

    @Column(nullable = false)
    private FriendRequestState status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser1(User user) {user1 = user;}
    public void setUser2(User user) {user2 = user;}
    public User getUser1() {
        return user1;
    }

    public User getUser2() {
        return user2;
    }

    public FriendRequestState getRequestStatus() {
        return status;
    }

    public void setRequestStatus(FriendRequestState status) {
        this.status = status;
    }
}