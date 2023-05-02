package local.springdemobot.modeldto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private boolean is_bot;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    private String username;
    @JsonProperty("language_code")
    private String languageCode;
}
