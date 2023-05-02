package local.springdemobot.modeldto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ReplyMarkupDto {
    private Boolean resize_keyboard;
    private Boolean one_time_keyboard;
    private Boolean remove_keyboard;
    private List<List<KeyDto>> keyboard;
}
