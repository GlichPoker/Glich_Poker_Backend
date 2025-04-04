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
    @JoinColumn(name = "user1Id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user2Id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user2;

    @Column(nullable = false)
    private FriendRequestState status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser1Id(Long user1Id) {user1.setId(user1Id);}
    public void setUser2Id(Long user2Id) {user2.setId(user2Id);}
    public Long getUser1Id() {
        return user1.getId();
    }

    public Long getUser2Id() {
        return user2.getId();
    }

    public FriendRequestState getRequestStatus() {
        return status;
    }

    public void setRequestStatus(FriendRequestState status) {
        this.status = status;
    }
}