package local.springdemobot.model;

import lombok.Data;

import java.util.List;
@Data
public class UpdatesDto {
    private Boolean ok;
    private List<UpdateDto> result;
}
