package local.springdemobot.database.entites;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(schema = "bot", name = "users")
@NoArgsConstructor
@Data
public class User {
    @Id
    private Long id;
    private String name;
    private String number;
    private String command;
    private String state;
    private Boolean auth;
    @OneToMany(mappedBy = "id")
    @ToString.Exclude
    private List<Document> documents;

    public User(Long id, String name, String number) {
        this.id = id;
        this.name = name;
        this.number = number;
    }

    public User(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public User(Long id) {
        this.id = id;
    }


    @Override
    public String toString() {
        return "Телеграм id: " + id + "\n" +
                "Имя: " + name + "\n" +
                "Номер: " + number;
    }
}
