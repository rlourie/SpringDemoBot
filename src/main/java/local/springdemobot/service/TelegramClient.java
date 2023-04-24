package local.springdemobot.service;

import local.springdemobot.model.OffsetStore;
import local.springdemobot.model.UpdateDto;
import local.springdemobot.model.UpdatesDto;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Component
@ConfigurationProperties(prefix = "springdemobot.telegram-client")
@Setter
public class TelegramClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private String botToken;
    private String telegramUrl;
    @Autowired
    private OffsetStore offsetStore;

    public List<UpdateDto> getUpdates() {
        Optional<Long> maybeOffset = offsetStore.tryReadOffset();

        return maybeOffset.map(this::getUpdates).orElseGet(Collections::emptyList);

    }

    private List<UpdateDto> getUpdates(Long offset) {
        String url = String.format("%s/bot%s/getUpdates?timeout=90&offset=%s", telegramUrl, botToken, offset);
        UpdatesDto response;
        try {
            response = restTemplate.getForObject(new URI(url), UpdatesDto.class);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return response != null ? response.getResult() : null;
    }

}
