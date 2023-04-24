package local.springdemobot.model;

import lombok.Data;

@Data
public class UpdateDto {
    private Long update_id;
    private MessageDto message;
}
