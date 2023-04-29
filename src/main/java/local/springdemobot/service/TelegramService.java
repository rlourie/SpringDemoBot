package local.springdemobot.service;

import local.springdemobot.database.entites.Document;
import local.springdemobot.database.entites.User;
import local.springdemobot.database.entites.UserStatus;
import local.springdemobot.database.repository.DocumentRepository;
import local.springdemobot.database.repository.UserRepository;
import local.springdemobot.database.repository.UserStatusRepository;
import local.springdemobot.model.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
@AllArgsConstructor
public class TelegramService {
    private OffsetStore offsetStore;
    private TelegramClient telegramClient;
    private UserStatusRepository userStatusRepository;
    private UserRepository userRepository;
    private DocumentRepository documentRepository;

    public void processing(List<UpdateDto> updates) {
        try {
            for (UpdateDto update : updates) {
                Long chatId = update.getMessage().getChat().getId();
                if (!(this.isStart(update)))
                    break;
                if (!(this.isAuth(update)))
                    break;
                switch (update.getType()) {
                    case TEXT:
                        log.info("Text");
                        this.processingText(update);
                        break;
                    case FILE:
                        log.info("File");
                        this.processingFile(update);
                        break;
                    case OTHER:
                        MessageSendDto warnMessage = new MessageSendDto(chatId, "Извините, я не понимаю" +
                                " вашу команду");
                        telegramClient.sendMessage(warnMessage);
                }
            }
        } catch (Exception e) {
            log.info(e.toString());
        }
        if (updates.size() > 0) {
            int lastOffset = updates.get(updates.size() - 1).getUpdate_id();
            offsetStore.setOffset((long) (lastOffset + 1));
        }
    }

    private void processingFile(UpdateDto update) {
        Long userId = update.getMessage().getFrom().getId();
        if (userStatusRepository.findByUserIdAndCommandUploadAndStatusProcessing(userId).isPresent()) {
            DocumentDto document = update.getMessage().getDocument();
            Document documentToSave = new Document();
            documentToSave.setId(document.getFile_id());
            documentToSave.setUnique_id(document.getFile_unique_id());
            documentToSave.setSize(document.getFile_size());
            documentToSave.setName(document.getFile_name());
            User user = new User(userId);
            documentToSave.setUser(user);
            documentRepository.save(documentToSave);
        }

    }

    private void processingText(UpdateDto update) {
        Long chatId = update.getMessage().getChat().getId();
        Long userId = update.getMessage().getFrom().getId();
        Optional<UserStatus> mayBeUserStatus = userStatusRepository.findByUserIdAndCommandUploadAndStatusProcessing(userId);
        if (mayBeUserStatus.isPresent()) {
            if (Objects.equals(update.getMessage().getText(), "Готово")) {
                MessageSendDto infoMessage = new MessageSendDto(chatId, "Файлы загруженны!");
                ReplyMarkupDto deleteKeyBoard = new ReplyMarkupDto();
                deleteKeyBoard.setRemove_keyboard(true);
                infoMessage.setReply_markup(deleteKeyBoard);
                telegramClient.sendMessage(infoMessage);
                UserStatus userStatus = mayBeUserStatus.get();
                userStatus.setStatus("done");
                userStatusRepository.save(userStatus);

                return;
            } else {
                MessageSendDto warnMessage = new MessageSendDto(chatId, "Нажмите на кнопку готово для" +
                        " завершения загрузки файлов");
                telegramClient.sendMessage(warnMessage);
                return;
            }
        }
        switch (update.getMessage().getText()) {
            case "/view":
                log.info("view");
                break;
            case "/upload":
                log.info("upload");
                telegramClient.uploadCommand(update.getMessage().getChat().getId());
                UserStatus userStatusUploadProcessing = new UserStatus();
                userStatusUploadProcessing.setUserId(update.getMessage().getFrom().getId());
                userStatusUploadProcessing.setCommand("/upload");
                userStatusUploadProcessing.setStatus("processing");
                userStatusRepository.save(userStatusUploadProcessing);
                break;
            case "/delete":
                log.info("delete");
                break;
            default:
                MessageSendDto warnMessage = new MessageSendDto(chatId, "Извините, я не понимаю вашу команду");
                telegramClient.sendMessage(warnMessage);

        }
    }

    private Boolean sharedPhone(MessageDto message) {
        Long userId = message.getFrom().getId();
        telegramClient.sendSharePhone(message.getChat().getId());
        User user = new User();
        user.setId(userId);
        user.setName(message.getFrom().getFirst_name());
        userRepository.save(user);
        UserStatus userStatus = new UserStatus();
        userStatus.setUserId(user.getId());
        userStatus.setStatus("auth");
        userStatusRepository.save(userStatus);
        return false;
    }

    private Boolean savePhone(MessageDto message) {
        Long userId = message.getFrom().getId();
        Long chatId = message.getChat().getId();
        if (!(this.isAnswerNumber(message))) {
            return false;
        }
        String phoneNumber = message.getContact().getPhone_number();
        User user = userRepository.findById(userId).orElse(new User(userId, message.getFrom().getFirst_name()));
        user.setNumber(phoneNumber);
        userRepository.save(user);
        ReplyMarkupDto deleteKeyBoard = new ReplyMarkupDto();
        deleteKeyBoard.setRemove_keyboard(true);
        MessageSendDto messageDeleteKeyBoard = new MessageSendDto(chatId, "Отлично твой номер сохранен");
        messageDeleteKeyBoard.setReply_markup(deleteKeyBoard);
        telegramClient.sendMessage(messageDeleteKeyBoard);
        UserStatus userStatus = new UserStatus();
        userStatus.setUserId(user.getId());
        userStatus.setStatus("auth_done");
        userStatusRepository.save(userStatus);
        return false;
    }

    private Boolean isStart(UpdateDto update) {
        Long userId = update.getMessage().getFrom().getId();
        String messageText = update.getMessage().getText();
        if (userRepository.findById(userId).isEmpty()) {
            return Objects.equals(messageText, "/start");
        }
        return true;
    }

    private Boolean isAuth(UpdateDto update) {
        Long userId = update.getMessage().getFrom().getId();
        if (userRepository.findById(userId).isEmpty()) {
            return this.sharedPhone(update.getMessage());
        } else if (userStatusRepository.findAllByIdAndStatusAuthDone(userId).isEmpty()) {
            return this.savePhone(update.getMessage());
        }
        return true;
    }

    private Boolean isAnswerNumber(MessageDto message) {
        Long chatId = message.getChat().getId();
        if (!((message.getReply_to_message() != null) && (message.getContact() != null))) {
            MessageSendDto warnMessage = new MessageSendDto(chatId, "Не корректный номер телефона, нажмите на" +
                    " кнопку поделиться контактом");
            telegramClient.sendMessage(warnMessage);
            return false;
        } else {
            return true;
        }
    }
}
