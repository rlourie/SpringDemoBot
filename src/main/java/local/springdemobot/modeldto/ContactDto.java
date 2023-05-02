package local.springdemobot.modeldto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ContactDto {
    @JsonProperty("phone_number")
    private String phoneNumber;
}
