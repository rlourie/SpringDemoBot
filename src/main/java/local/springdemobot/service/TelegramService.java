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
                if (!(this.isStart(update.getMessage())))
                    break;
                if (!(this.isAuth(update.getMessage())))
                    break;
                switch (update.getType()) {
                    case TEXT:
                        log.info("Text");
                        this.processingText(update.getMessage());
                        break;
                    case FILE:
                        log.info("File");
                        this.processingFile(update.getMessage());
                        break;
                    case OTHER:
                        telegramClient.understandCommand(chatId);
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

    private void processingFile(MessageDto message) {
        Long userId = message.getFrom().getId();
        if (userStatusRepository.findByUserIdAndCommandUploadAndStatusProcessing(userId).isPresent()) {
            DocumentDto document = message.getDocument();
            Document documentToSave = new Document(document.getFile_id(), document.getFile_unique_id(),
                    document.getFile_size(), document.getFile_name(), new User(userId));
            documentRepository.save(documentToSave);
        }

    }

    private void processingText(MessageDto message) {
        Long chatId = message.getChat().getId();
        if (mayBeUploadDone(message)) {
            return;
        }
        switch (message.getText()) {
            case "/view":
                log.info("view");
                break;
            case "/upload":
                log.info("upload");
                saveUserStatusProcessingCommandUpload(message);
                break;
            case "/delete":
                log.info("delete");
                break;
            default:
                telegramClient.understandCommand(chatId);

        }
    }

    private void saveUserStatusProcessingCommandUpload(MessageDto message) {
        telegramClient.uploadCommand(message.getChat().getId());
        UserStatus userStatusUploadProcessing = new UserStatus
                (message.getFrom().getId(), "processing", "/upload");
        userStatusRepository.save(userStatusUploadProcessing);
    }

    private Boolean mayBeUploadDone(MessageDto message) {
        Long userId = message.getFrom().getId();
        Long chatId = message.getChat().getId();
        Optional<UserStatus> mayBeUserStatus = userStatusRepository.
                findByUserIdAndCommandUploadAndStatusProcessing(userId);
        if (mayBeUserStatus.isPresent()) {
            if (Objects.equals(message.getText(), "Готово")) {
                telegramClient.uploadDone(chatId);
                UserStatus userStatus = mayBeUserStatus.get();
                userStatus.setStatus("done");
                userStatusRepository.save(userStatus);
                return true;
            } else {
                telegramClient.uploadPressDonePlease(chatId);
                return true;
            }
        } else {
            return false;
        }
    }

    private Boolean sharedPhone(MessageDto message) {
        Long userId = message.getFrom().getId();
        String name = message.getFrom().getFirst_name() + ' ' + message.getFrom().getLast_name() +
                " @" + message.getFrom().getUsername();
        telegramClient.sendSharePhone(message.getChat().getId());
        User user = new User(userId, name);
        userRepository.save(user);
        UserStatus userStatus = new UserStatus(user.getId(), "auth");
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
        String name = message.getFrom().getFirst_name() + ' ' + message.getFrom().getLast_name() +
                " @" + message.getFrom().getUsername();
        User user = userRepository.findById(userId).orElse(new User(userId, name, phoneNumber));
        userRepository.save(user);
        telegramClient.savePhone(chatId);
        UserStatus userStatus = new UserStatus(user.getId(), "auth_done");
        userStatusRepository.save(userStatus);
        return false;
    }

    private Boolean isStart(MessageDto message) {
        Long userId = message.getFrom().getId();
        String messageText = message.getText();
        if (userRepository.findById(userId).isEmpty()) {
            return Objects.equals(messageText, "/start");
        }
        return true;
    }

    private Boolean isAuth(MessageDto message) {
        Long userId = message.getFrom().getId();
        if (userRepository.findById(userId).isEmpty()) {
            return this.sharedPhone(message);
        } else if (userStatusRepository.findAllByIdAndStatusAuthDone(userId).isEmpty()) {
            return this.savePhone(message);
        }
        return true;
    }

    private Boolean isAnswerNumber(MessageDto message) {
        Long chatId = message.getChat().getId();
        if (!((message.getReply_to_message() != null) && (message.getContact() != null))) {
            telegramClient.incorrectPhone(chatId);
            return false;
        } else {
            return true;
        }
    }
}
