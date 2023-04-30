package local.springdemobot.database.entites;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(schema = "bot", name = "users_status")
@Data
@NoArgsConstructor
public class UserStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne()
    @ToString.Exclude
    private User user;
    private String command;
    private String status;


    public UserStatus(User user, String status, String command) {
        this.user = user;
        this.status = status;
        this.command = command;
    }

    public UserStatus(User user, String status) {
        this.user = user;
        this.status = status;
    }

}
