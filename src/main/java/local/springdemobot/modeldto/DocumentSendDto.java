package local.springdemobot.modeldto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DocumentSendDto {
    Long chat_id;
    String caption;
    String document;
}
