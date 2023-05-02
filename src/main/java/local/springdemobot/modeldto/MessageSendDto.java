package local.springdemobot.modeldto;

import lombok.Data;

@Data
public class MessageSendDto {
    private final Long chat_id;
    private final String text;
    private  ReplyMarkupDto reply_markup;

}
