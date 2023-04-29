package local.springdemobot.database.entites;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
@Entity
@Table(schema = "bot", name = "document")
@ToString
@Data
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
