package local.springdemobot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DocumentSendDto {
    Long chat_id;
    String caption;
    String document;
}
