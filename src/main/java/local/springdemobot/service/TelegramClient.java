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

    public void uploadCommand(Long chatId) {
        KeyDto doneKey = new KeyDto("Готово");

        List<KeyDto> inerList = new ArrayList<>();
        inerList.add(doneKey);
        List<List<KeyDto>> listKey = new ArrayList<>();
        listKey.add(inerList);

        ReplyMarkupDto replyMarkupDto = new ReplyMarkupDto();
        replyMarkupDto.setKeyboard(listKey);
        replyMarkupDto.setOne_time_keyboard(true);
        replyMarkupDto.setResize_keyboard(true);

        MessageSendDto uploadMessage = new MessageSendDto(chatId, "Прикрипите ваши файлы для загрузки и" +
                " нажмите на кнопку готово");
        uploadMessage.setReply_markup(replyMarkupDto);
        sendMessage(uploadMessage);

    }

    public void sendMessage(MessageSendDto messageSendDto) {
        String url = String.format("%s/bot%s/sendMessage", telegramUrl, botToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Gson gson = new GsonBuilder().create();
        HttpEntity<String> request = new HttpEntity<>(gson.toJson(messageSendDto), headers);
        restTemplate.postForEntity(url, request, String.class);
    }

    public void understandCommand(Long chatId) {
        MessageSendDto warnMessage = new MessageSendDto(chatId, "Извините, я не понимаю вашу команду");
        sendMessage(warnMessage);
    }

    public void uploadDone(Long chatId) {
        MessageSendDto infoMessage = new MessageSendDto(chatId, "Файлы загруженны!");
        ReplyMarkupDto deleteKeyBoard = new ReplyMarkupDto();
        deleteKeyBoard.setRemove_keyboard(true);
        infoMessage.setReply_markup(deleteKeyBoard);
        sendMessage(infoMessage);
    }

    public void uploadPressDonePlease(Long chatId) {
        MessageSendDto warnMessage = new MessageSendDto(chatId, "Нажмите на кнопку готово для" +
                " завершения загрузки файлов");
        sendMessage(warnMessage);
    }

    public void savePhone(Long chatId) {
        ReplyMarkupDto deleteKeyBoard = new ReplyMarkupDto();
        deleteKeyBoard.setRemove_keyboard(true);
        MessageSendDto messageDeleteKeyBoard = new MessageSendDto(chatId, "Отлично твой номер сохранен");
        messageDeleteKeyBoard.setReply_markup(deleteKeyBoard);
        sendMessage(messageDeleteKeyBoard);
    }

    public void incorrectPhone(Long chatId) {
        MessageSendDto warnMessage = new MessageSendDto(chatId, "Не корректный номер телефона, нажмите на" +
                " кнопку поделиться контактом");
        sendMessage(warnMessage);
    }
}
