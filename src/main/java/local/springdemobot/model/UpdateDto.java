package local.springdemobot.model;

import local.springdemobot.enums.TypeUpdate;
import lombok.Data;

@Data
public class UpdateDto {
    private int update_id;
    private MessageDto message;

    public TypeUpdate getType() {
        if (message.getText() != null)
            return TypeUpdate.TEXT;
        if (message.getDocument() != null)
            return TypeUpdate.FILE;
        return TypeUpdate.OTHER;
    }
}
