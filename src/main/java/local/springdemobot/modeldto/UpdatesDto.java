package local.springdemobot.modeldto;

import lombok.Data;

import java.util.List;
@Data
public class UpdatesDto {
    private Boolean ok;
    private List<UpdateDto> result;
}
