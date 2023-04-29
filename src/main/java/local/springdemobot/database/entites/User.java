package local.springdemobot.database.entites;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.lang.Nullable;

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
    @OneToMany(mappedBy = "id")
    @ToString.Exclude
    private List<Document> documents;

    public User(Long userId, String firstName) {
        this.id = userId;
        this.name = firstName;
    }

    public User(Long userId) {
        this.id = userId;
    }

    public User() {

    }

}
