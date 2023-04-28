package local.springdemobot.model;

import lombok.Data;

@Data
public class DocumentDto {
    String file_name;
    String file_id;
    String file_unique_id;
    String file_size;
}
