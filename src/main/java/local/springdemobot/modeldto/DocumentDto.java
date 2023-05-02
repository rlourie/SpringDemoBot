package local.springdemobot.modeldto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DocumentDto {
    @JsonProperty("file_name")
    String fileName;
    @JsonProperty("file_id")
    String fileId;
    @JsonProperty("file_unique_id")
    String fileUniqueId;
    @JsonProperty("file_size")
    String fileSize;
    String caption;
}
