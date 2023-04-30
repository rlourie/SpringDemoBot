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
import java.util.stream.Collectors;

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
                if (!(isStart(update.getMessage())))
                    break;
                if (!(isAuth(update.getMessage())))
                    break;
                switch (update.getType()) {
                    case TEXT:
                        processingText(update.getMessage());
                        break;
                    case FILE:
                        processingFile(update.getMessage());
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
        boolean upload = userStatusRepository.findByUserIdAndCommandUploadAndStatusProcessing(userId).isPresent();
        if (upload) {
            DocumentDto document = message.getDocument();
            Document documentToSave = new Document(document.getFile_id(), document.getFile_name(), document.getFile_unique_id(),
                    document.getFile_size(), new User(userId));
            documentRepository.save(documentToSave);
        }
    }

    private void processingText(MessageDto message) {
        Long chatId = message.getChat().getId();
        Long userId = message.getFrom().getId();
        boolean delete = userStatusRepository.findByUserIdAndCommandDeleteAndStatusProcessing(userId).isPresent();
        if (mayBeUploadDone(message)) {
            return;
        }
        if (mayBeDeleteDone(message)) {
            return;
        }
        if (delete) {
            deleteFiles(message);
            return;
        }
        switch (message.getText()) {
            case "/view":
                viewFiles(message, true);
                break;
            case "/upload":
                uploadFilesStart(message);
                break;
            case "/delete":
                deleteFilesStart(message);
                break;
            default:
                telegramClient.understandCommand(chatId);

        }
    }

    private void deleteFiles(MessageDto message) {
        if (documentRepository.findByUserIdAndFileName(
                message.getFrom().getId(), message.getText()).isPresent()) {
            Document documentForDelete = documentRepository.findByUserIdAndFileName(
                    message.getFrom().getId(), message.getText()).get();
            documentRepository.delete(documentForDelete);
        } else {
            telegramClient.nonExistentFile(message.getChat().getId());
        }

    }

    private void deleteFilesStart(MessageDto message) {
        List<String> documentNameList = documentRepository.findByUserId(message.getFrom().getId()).stream()
                .map(Document::getName)
                .collect(Collectors.toList());
        if (documentNameList.isEmpty()) {
            telegramClient.emptyFile(message.getChat().getId());
            return;
        }
        telegramClient.deleteCommand(message.getChat().getId(), documentNameList);
        viewFiles(message, false);
        UserStatus userStatusDeleteProcessing = new UserStatus
                (message.getFrom().getId(), "processing", "/delete");
        userStatusRepository.save(userStatusDeleteProcessing);
    }

    private void viewFiles(MessageDto message, Boolean all) {
        List<Document> documentList;
        if (all) {
            documentList = documentRepository.findAll();
        } else {
            documentList = documentRepository.findByUserId(message.getFrom().getId());
        }
        List<DocumentSendDto> documentSendDtoList = documentList.stream()
                .map(document -> new DocumentSendDto(
                        message.getChat().getId(),
                        document.getUser().getName(),
                        document.getId())
                )
                .collect(Collectors.toList());
        if (documentSendDtoList.size() == 0) {
            telegramClient.emptyFile(message.getChat().getId());
        }
        for (DocumentSendDto document : documentSendDtoList) {
            telegramClient.sendDocument(document);
        }

    }

    private void uploadFilesStart(MessageDto message) {
        telegramClient.uploadCommand(message.getChat().getId());
        UserStatus userStatusUploadProcessing = new UserStatus
                (message.getFrom().getId(), "processing", "/upload");
        userStatusRepository.save(userStatusUploadProcessing);
    }

    private Boolean mayBeUploadDone(MessageDto message) {
        Long userId = message.getFrom().getId();
        String uploadDone = "Файлы загруженны!";
        Optional<UserStatus> mayBeUserStatus = userStatusRepository.
                findByUserIdAndCommandUploadAndStatusProcessing(userId);
        if (mayBeUserStatus.isPresent() && !Objects.equals(message.getText(), "Готово")) {
            telegramClient.incorrectFile(message.getChat().getId());
            return true;
        }
        mayBeUserStatus
                .map(userStatus -> mayBeAnyDone(message, userStatus, uploadDone));
        return mayBeUserStatus.isPresent();
    }

    private boolean mayBeDeleteDone(MessageDto message) {
        Long userId = message.getFrom().getId();
        String deleteDone = "Файлы удалены!";
        Optional<UserStatus> mayBeUserStatus = userStatusRepository.
                findByUserIdAndCommandDeleteAndStatusProcessing(userId);
        if (mayBeUserStatus.isPresent() && documentRepository.findByUserIdAndFileName(
                message.getFrom().getId(), message.getText()).isPresent()) {
            return false;
        } else if (documentRepository.findByUserIdAndFileName(
                message.getFrom().getId(), message.getText()).isEmpty() &&
                !(Objects.equals(message.getText(), "Готово"))) {
            return false;
        }
        return mayBeUserStatus
                .map(userStatus -> mayBeAnyDone(message, userStatus, deleteDone))
                .orElse(false);
    }

    private Boolean mayBeAnyDone(MessageDto message, UserStatus status, String commandDone) {
        telegramClient.commandDone(message.getChat().getId(), commandDone);
        status.setStatus("done");
        userStatusRepository.save(status);
        return true;
    }

    private Boolean sharedPhone(MessageDto message) {
        Long userId = message.getFrom().getId();
        String name = message.getFrom().getFirst_name() + ' ' + message.getFrom().getLast_name() +
                " @" + message.getFrom().getUsername();
        telegramClient.sharePhone(message.getChat().getId());
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
        //!((message.getReply_to_message() != null) &&
        if (message.getContact() == null) {
            telegramClient.incorrectPhone(chatId);
            return false;
        } else {
            return true;
        }
    }
}
