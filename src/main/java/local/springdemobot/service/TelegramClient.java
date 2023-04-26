package local.springdemobot.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import local.springdemobot.model.*;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


@ToString
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

    public void sendSharePhone(Long chatId) {
        KeyDto sharedKey = new KeyDto("Поделиться номером", true);

        List<KeyDto> inerList = new ArrayList<>();
        inerList.add(sharedKey);
        List<List<KeyDto>> listKey = new ArrayList<>();
        listKey.add(inerList);

        ReplyMarkupDto replyMarkupDto = new ReplyMarkupDto();
        replyMarkupDto.setKeyboard(listKey);
        replyMarkupDto.setOne_time_keyboard(true);
        replyMarkupDto.setResize_keyboard(true);

        MessageSendDto authMessage = new MessageSendDto(chatId, "Авторизуйся что бы использовать функции бота");
        authMessage.setReply_markup(replyMarkupDto);

        sendMessage(authMessage);
    }

    public void sendMessage(MessageSendDto messageSendDto) {
        String url = String.format("%s/bot%s/sendMessage", telegramUrl, botToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Gson gson = new GsonBuilder().create();
        HttpEntity<String> request = new HttpEntity<>(gson.toJson(messageSendDto), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
    }
}
