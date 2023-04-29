package local.springdemobot.database.entites;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
@Entity
@Table(schema = "bot", name = "document")
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Document {
    @Id
    private String id;
    private String name;
    private String unique_id;
    private String size;
    @ManyToOne()
    @ToString.Exclude
    private User user;

}
