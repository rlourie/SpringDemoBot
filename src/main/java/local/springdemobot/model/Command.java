package local.springdemobot.model;

import local.springdemobot.enums.TypeUserCommands;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@AllArgsConstructor
@NoArgsConstructor
public class Command {
    private TypeUserCommands typeCommand;
    private List<String> args;
}
