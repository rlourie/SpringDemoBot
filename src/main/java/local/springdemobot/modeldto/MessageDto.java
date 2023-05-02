package local.springdemobot.modeldto;

import lombok.Data;

@Data
public class MessageDto {
    private int message_id;
    private UserDto from;
    private ChatDto chat;
    private int date;
    private String text;
    private DocumentDto document;
    private ContactDto contact;
    private MessageDto reply_to_message;
}
