package local.springdemobot.modeldto;

import lombok.Data;

@Data
public class KeyDto {
    private String text;
    private Boolean request_contact;

    public KeyDto(String text) {
        this.text = text;
    }

    public KeyDto(String text, boolean request_contact) {
        this.text = text;
        this.request_contact = request_contact;
    }
}
