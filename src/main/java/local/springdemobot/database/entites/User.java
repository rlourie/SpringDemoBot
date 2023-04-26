package local.springdemobot.database.entites;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(schema = "bot", name = "users")
@ToString
@Data
public class User {
    @Id
    private Long id;
    private String name;
    private String number;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "id")
    })
    @ToString.Exclude
    private List<UserStatus> userStatuses;


}
