package local.springdemobot.model;

import lombok.Data;

@Data
public class ChatDto {
    private int id;
    private String first_name;
    private String last_name;
    private String username;
    private String type;
}
