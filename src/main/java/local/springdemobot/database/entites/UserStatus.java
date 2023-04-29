package local.springdemobot.database.entites;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(schema = "bot", name = "users_status")
@Data
public class UserStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String command;
    private String status;


    public UserStatus(Long userId, String status, String command) {
        this.userId = userId;
        this.status = status;
        this.command = command;
    }

    public UserStatus(Long userId, String status) {
        this.userId = userId;
        this.status = status;
    }

}
