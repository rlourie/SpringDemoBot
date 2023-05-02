package local.springdemobot.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import local.springdemobot.database.entites.User;
import local.springdemobot.model.OffsetStore;
import local.springdemobot.modeldto.*;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


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

    public void sendMessage(MessageSendDto message) {
        String url = String.format("%s/bot%s/sendMessage", telegramUrl, botToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Gson gson = new GsonBuilder().create();
        HttpEntity<String> request = new HttpEntity<>(gson.toJson(message), headers);
        restTemplate.postForEntity(url, request, String.class);
    }

    public void sendDocument(DocumentSendDto document) {
        String url = String.format("%s/bot%s/sendDocument", telegramUrl, botToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Gson gson = new GsonBuilder().create();
        HttpEntity<String> request = new HttpEntity<>(gson.toJson(document), headers);
        restTemplate.postForEntity(url, request, String.class);

    }

    public void sendUser(User user, Long chatId) {
        MessageSendDto userMessage = new MessageSendDto(chatId, user.toString());
        sendMessage(userMessage);
    }

    public void sharePhone(Long chatId) {
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

    public void deleteCommand(Long chatId, List<String> nameList, String text) {
        List<KeyDto> inerList = new ArrayList<>();
        List<List<KeyDto>> listKey = new ArrayList<>();
        listKey.add(inerList);
        for (String name : nameList) {
            KeyDto key = new KeyDto(name);
            inerList.add(key);
        }
        inerList.add(new KeyDto("Готово"));
        ReplyMarkupDto replyMarkupDto = new ReplyMarkupDto();
        replyMarkupDto.setKeyboard(listKey);
        replyMarkupDto.setOne_time_keyboard(true);
        replyMarkupDto.setResize_keyboard(true);

        MessageSendDto deleteMessage = new MessageSendDto(chatId, text);
        deleteMessage.setReply_markup(replyMarkupDto);
        sendMessage(deleteMessage);
    }

    public void nonUnderstandCommand(Long chatId) {
        MessageSendDto warnMessage = new MessageSendDto(chatId, "Извините, я не понимаю вашу команду");
        sendMessage(warnMessage);
    }

    public void commandDone(Long chatId, String text) {
        MessageSendDto infoMessage = new MessageSendDto(chatId, text);
        ReplyMarkupDto deleteKeyBoard = new ReplyMarkupDto();
        deleteKeyBoard.setRemove_keyboard(true);
        infoMessage.setReply_markup(deleteKeyBoard);
        sendMessage(infoMessage);
    }

    public void incorrectPhone(Long chatId) {
        MessageSendDto warnMessage = new MessageSendDto(chatId, "Не корректный номер телефона, нажмите на" +
                " кнопку поделиться контактом");
        sendMessage(warnMessage);
    }

    public void nonExistentFile(Long chatId) {
        MessageSendDto warnMessage = new MessageSendDto(chatId, "Такого файла не сущетсвует, " +
                "выбирете из предоставленных вам");
        sendMessage(warnMessage);
    }

    public void emptyFile(Long chatId) {
        MessageSendDto infoMessage = new MessageSendDto(chatId, "К сожалению файлов пока нету (((");
        sendMessage(infoMessage);
    }

    public void incorrectFile(Long chatId) {
        MessageSendDto warnMessage = new MessageSendDto(chatId, "Некоректный файл");
        sendMessage(warnMessage);
    }

    public void incorrectUserId(Long chatId) {
        MessageSendDto warnMessage = new MessageSendDto(chatId, "Некоректный id пользователя");
        sendMessage(warnMessage);
    }

    public void unAuth(Long chatId) {
        MessageSendDto warnMessage = new MessageSendDto(chatId, "Вы не смогли пройти авторизацию " +
                "поробуйте снова");
        sendMessage(warnMessage);
    }
}
