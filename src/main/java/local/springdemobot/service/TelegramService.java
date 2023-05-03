package local.springdemobot.service;

import local.springdemobot.database.entites.Numbers;
import local.springdemobot.model.Command;
import local.springdemobot.model.SetBot;
import local.springdemobot.database.entites.Document;
import local.springdemobot.database.entites.User;
import local.springdemobot.database.repository.DocumentRepository;
import local.springdemobot.database.repository.NumberRepository;
import local.springdemobot.database.repository.UserRepository;
import local.springdemobot.enums.TypeUserCommands;
import local.springdemobot.enums.TypeStates;
import local.springdemobot.model.CommandBuilder;
import local.springdemobot.model.OffsetStore;
import local.springdemobot.modeldto.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
@Slf4j
@AllArgsConstructor
public class TelegramService {
    private OffsetStore offsetStore;
    private SetBot bot;
    private TelegramClient telegramClient;
    private UserRepository userRepository;
    private DocumentRepository documentRepository;
    private NumberRepository numberRepository;
    private CommandBuilder commandBuilder;

    @PostConstruct
    private void postSetting() {
        commandBuilder.setAdminPostfix(bot.getBotUserName());
    }


    public void processing(List<UpdateDto> updates) {
        try {
            for (UpdateDto update : updates) {
                if (Objects.equals(update.getMessage().getChat().getId(), bot.getAdminGroupId())) {
                    processingAdmin(update);
                } else {
                    processingUser(update);
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

    private void processingUser(UpdateDto update) {
        Long chatId = update.getMessage().getChat().getId();
        if (!(isStart(update.getMessage())))
            return;
        if (!(isAuth(update.getMessage())))
            return;
        switch (update.getType()) {
            case TEXT:
                processingText(update.getMessage());
                break;
            case FILE:
                processingFile(update.getMessage());
                break;
            case OTHER:
                telegramClient.nonUnderstandCommand(chatId);
        }

    }

    private void processingAdmin(UpdateDto update) {
        Command adminCommand = commandBuilder.parseToCommand(update.getMessage());
        switch (adminCommand.getTypeCommand()) {
            case VIEW:
                viewAllUser();
                break;
            case CREATE:
                createNumber(adminCommand);
                break;
            case DELETE:
                deleteNumberAndUser(adminCommand);
                break;

        }

    }

    //*ADMIN*

    private void viewAllUser() {
        List<User> userList = userRepository.findAll();
        List<Numbers> numberList = numberRepository.findAll();
        telegramClient.sendMessage(new MessageSendDto(bot.getAdminGroupId(), "ПОЛЬЗОВАТЕЛИ:"));
        for (User user : userList) {
            telegramClient.sendUser(user, bot.getAdminGroupId());
        }
        telegramClient.sendMessage(new MessageSendDto(bot.getAdminGroupId(), "ТЕЛЕФОНЫ:"));
        for (Numbers number : numberList) {
            telegramClient.sendMessage(new MessageSendDto(bot.getAdminGroupId(), number.getNumber()));
        }
    }

    private void createNumber(Command adminCommand) {
        Numbers number = new Numbers();
        if (Objects.equals(adminCommand.getArgs().get(0), "")) {
            telegramClient.commandDone(bot.getAdminGroupId(), "Некоректный номер");
            return;
        }
        number.setNumber(adminCommand.getArgs().get(0));
        numberRepository.save(number);
        telegramClient.commandDone(bot.getAdminGroupId(), "Номер сохранен");
    }

    private void deleteNumberAndUser(Command adminCommand) {
        if (userRepository.findByNumber(adminCommand.getArgs().get(0)).isEmpty()) {
            telegramClient.incorrectUserPhoneAdmin(bot.getAdminGroupId());
        } else {
            User user = userRepository.findByNumber(adminCommand.getArgs().get(0)).get();
            documentRepository.deleteByUserId(user.getId());
            userRepository.delete(user);
            telegramClient.commandDone(bot.getAdminGroupId(), "Пользователь удалён");
        }
        if (numberRepository.findByNumber(adminCommand.getArgs().get(0)).isEmpty()) {
            telegramClient.incorrectPhoneAdmin(bot.getAdminGroupId());
        } else {
            Numbers numbers = numberRepository.findByNumber(adminCommand.getArgs().get(0)).get();
            numberRepository.delete(numbers);
            telegramClient.commandDone(bot.getAdminGroupId(), "Телефон удалён");
        }

    }


    //*USER*
    private void processingFile(MessageDto message) {
        Long userId = message.getFrom().getId();
        boolean upload = userRepository.findByIdAndCommandAndState(
                userId,
                TypeUserCommands.UPLOAD.getTitle(),
                TypeStates.PROCESSING.getTitle()).isPresent();
        if (upload) {
            uploadFiles(message);
        }
    }

    private void processingText(MessageDto message) {
        Long chatId = message.getChat().getId();
        Long userId = message.getFrom().getId();
        boolean delete = userRepository.findByIdAndCommandAndState(
                userId,
                TypeUserCommands.DELETE.getTitle(),
                TypeStates.PROCESSING.getTitle()).isPresent();
        if (isUploadDone(message)) {
            return;
        }
        if (isDeleteDone(message)) {
            return;
        }
        if (delete) {
            deleteFiles(message);
            return;
        }
        Command command = commandBuilder.parseToCommand(message);
        switch (command.getTypeCommand()) {
            case VIEW:
                viewFiles(message, true);
                break;
            case UPLOAD:
                uploadFilesStart(message);
                break;
            case DELETE:
                deleteFilesStart(message);
                break;
            default:
                telegramClient.nonUnderstandCommand(chatId);

        }
    }

    // DELETE
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
        telegramClient.deleteCommand(message.getChat().getId(), documentNameList, "Какие файлы вы хотите удалить?");
        viewFiles(message, false);
        User userDeleteProcessing = userRepository.findById(message.getFrom().getId())
                .orElse(new User(message.getFrom().getId()));
        userDeleteProcessing.setCommand(TypeUserCommands.DELETE.getTitle());
        userDeleteProcessing.setState("processing");
        userRepository.save(userDeleteProcessing);
    }

    private boolean isDeleteDone(MessageDto message) {
        Long userId = message.getFrom().getId();
        String deleteDone = "Файлы удалены!";
        Optional<User> userDeleteProcessing = userRepository.
                findByIdAndCommandAndState(userId,
                        TypeUserCommands.DELETE.getTitle(),
                        TypeStates.PROCESSING.getTitle());
        if (userDeleteProcessing.isPresent() && documentRepository.findByUserIdAndFileName(
                userId,
                message.getText()).isPresent()) {
            return false;
        }
        if (documentRepository.findByUserIdAndFileName(
                userId,
                message.getText()).isEmpty() &&
                !(Objects.equals(message.getText(), "Готово"))) {
            return false;
        }
        if (userDeleteProcessing.isPresent() && Objects.equals(message.getText(), "Готово")) {
            saveDoneState(message, userDeleteProcessing.get(), deleteDone);
            return true;
        }
        return false;
    }


    // VIEW
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


    // UPLOAD
    private void uploadFiles(MessageDto message) {
        DocumentDto document = message.getDocument();
        Document documentToSave = new Document(document.getFileId(), document.getFileName(), document.getFileUniqueId(),
                document.getFileSize(), new User(message.getFrom().getId()));
        documentRepository.save(documentToSave);
    }

    private void uploadFilesStart(MessageDto message) {
        telegramClient.uploadCommand(message.getChat().getId());
        User userUploadProcessing = userRepository.findById(message.getFrom().getId())
                .orElse(new User(message.getFrom().getId()));
        userUploadProcessing.setCommand(TypeUserCommands.UPLOAD.getTitle());
        userUploadProcessing.setState(TypeStates.PROCESSING.getTitle());
        userRepository.save(userUploadProcessing);
    }

    private Boolean isUploadDone(MessageDto message) {
        Long userId = message.getFrom().getId();
        String uploadDone = "Файлы загруженны!";
        Optional<User> processingUploadUser = userRepository.findByIdAndCommandAndState(userId,
                TypeUserCommands.UPLOAD.getTitle(),
                TypeStates.PROCESSING.getTitle());
        if (processingUploadUser.isPresent() && !Objects.equals(message.getText(), "Готово")) {
            telegramClient.incorrectFile(message.getChat().getId());
            return true;
        }
        if (processingUploadUser.isPresent() && Objects.equals(message.getText(), "Готово")) {
            saveDoneState(message, processingUploadUser.get(), uploadDone);
            return true;
        }
        return false;

    }


    private void saveDoneState(MessageDto message, User user, String commandDone) {
        telegramClient.commandDone(message.getChat().getId(), commandDone);
        user.setState(TypeStates.DONE.getTitle());
        userRepository.save(user);
    }

    private void sharedPhone(MessageDto message) {
        Long userId = message.getFrom().getId();
        String userName = message.getFrom().getFirstName() + ' ' + message.getFrom().getLastName() +
                " @" + message.getFrom().getUsername();
        telegramClient.sharePhone(message.getChat().getId());
        User user = new User(userId, userName);
        user.setCommand(TypeUserCommands.AUTH.getTitle());
        user.setState(TypeStates.PROCESSING.getTitle());
        userRepository.save(user);
    }

    private void tryAuth(MessageDto message) {
        Long userId = message.getFrom().getId();
        Long chatId = message.getChat().getId();
        String authDone = "Вы авторизовались!";
        if (isNotNumber(message)) {
            return;
        }
        String phoneNumber = message.getContact().getPhoneNumber();
        String name = message.getFrom().getFirstName() + ' ' + message.getFrom().getLastName() +
                " @" + message.getFrom().getUsername();
        User user = userRepository.findById(userId).orElse(new User(userId, name, phoneNumber));
        user.setNumber(phoneNumber);
        user.setName(name);
        if (numberRepository.findByNumber(phoneNumber).isPresent()) {
            user.setAuth(true);
            saveDoneState(message, user, authDone);
            return;
        }
        userRepository.save(user);
        telegramClient.unAuth(chatId);
    }

    private Boolean isStart(MessageDto message) {
        Long userId = message.getFrom().getId();
        String messageText = message.getText();
        if (userRepository.findById(userId).isEmpty()) {
            return Objects.equals(messageText, TypeUserCommands.START.getTitle());
        }
        return true;
    }

    private Boolean isAuth(MessageDto message) {
        Long userId = message.getFrom().getId();
        if (userRepository.findById(userId).isEmpty()) {
            sharedPhone(message);
            return false;
        } else if ((userRepository.findById(userId).get().getAuth()) == null
                || !userRepository.findById(userId).get().getAuth()) {
            tryAuth(message);
            return false;
        }
        return true;
    }

    private Boolean isNotNumber(MessageDto message) {
        Long chatId = message.getChat().getId();
        if (message.getContact() == null) {
            telegramClient.incorrectPhone(chatId);
            return true;
        } else {
            return false;
        }
    }

}
