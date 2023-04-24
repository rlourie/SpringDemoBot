package local.springdemobot.database.entites;
import javax.persistence.*;

@Entity
@Table(schema = "bot", name = "users_status")
public class UserStatus {
    @Id
    @GeneratedValue
    private int id;
    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    private User user;
    private String command;
    private String status;

    public UserStatus() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
