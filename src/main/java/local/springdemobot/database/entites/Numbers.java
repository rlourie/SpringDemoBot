package local.springdemobot.database.entites;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(schema = "bot", name = "numbers")
public class Numbers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String number;
}
