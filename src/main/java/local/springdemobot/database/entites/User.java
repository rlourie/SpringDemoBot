package local.springdemobot.database.entites;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(schema = "bot", name = "users")
@ToString
@NoArgsConstructor
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

    public User(Long userId, String name, String number) {
        this.id = userId;
        this.name = name;
        this.number = number;
    }

    public User(Long userId, String name) {
        this.id = userId;
        this.name = name;
    }

    public User(Long userId) {
        this.id = userId;
    }


}
