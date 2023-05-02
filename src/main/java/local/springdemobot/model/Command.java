package local.springdemobot.model;

import local.springdemobot.enums.TypeCommands;
import local.springdemobot.modeldto.MessageDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Getter
@Setter
@Component
@AllArgsConstructor
@NoArgsConstructor
public class Command {
    TypeCommands command;
    String args;

    public TypeCommands getCommand(MessageDto message) {
        if (Objects.equals(message.getText(), TypeCommands.VIEW.getTitle()))
            return TypeCommands.VIEW;
        if (Objects.equals(message.getText(), TypeCommands.DELETE.getTitle()))
            return TypeCommands.DELETE;
        if (Objects.equals(message.getText(), TypeCommands.UPLOAD.getTitle()))
            return TypeCommands.UPLOAD;
        return TypeCommands.OTHER;
    }
}
