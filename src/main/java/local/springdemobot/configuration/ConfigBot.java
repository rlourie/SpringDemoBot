package local.springdemobot.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "springdemobot.telegram-client")
@Getter
@Setter
@Component
public class ConfigBot {
    private Long adminGroupId;
    private String botUserName;
}
