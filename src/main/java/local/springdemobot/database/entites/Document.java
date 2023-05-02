package local.springdemobot.database.entites;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(schema = "bot", name = "document")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Document {
    @Id
    private String id;
    private String name;
    @JsonProperty("unique_id")
    private String uniqueId;
    private String size;
    @ManyToOne()
    private User user;
}
