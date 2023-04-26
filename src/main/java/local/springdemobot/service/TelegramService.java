package local.springdemobot.service;

import local.springdemobot.database.entites.User;
import local.springdemobot.database.entites.UserStatus;
import local.springdemobot.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class TelegramService {
    @Autowired
    private OffsetStore offsetStore;
    @Autowired
    private DbClient dbClient;
    @Autowired
    private TelegramClient telegramClient;

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
                        MessageSendDto messageSendDto = new MessageSendDto(chatId,
                                update.getMessage().getText());
                        telegramClient.sendMessage(messageSendDto);
                        break;
                    case FILE:
                        log.info("File");
                        break;
                    case OTHER:
                        log.info("Other");
                        break;
                }
            }
        } catch (Exception ignored) {
            log.info(ignored.toString());
        }
        if (updates.size() > 0) {
            int lastOffset = updates.get(updates.size() - 1).getUpdate_id();
            offsetStore.setOffset((long) (lastOffset + 1));
        }
    }

    private Boolean isStart(UpdateDto update) {
        Long userId = update.getMessage().getFrom().getId();
        String messageText = update.getMessage().getText();
        if (dbClient.findUserById(userId).isEmpty()) {
            return Objects.equals(messageText, "/start");
        }
        return true;
    }

    private Boolean isAuth(UpdateDto update) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChat().getId();
        if (dbClient.findUserById(userId).isEmpty()) {
            telegramClient.sendSharePhone(update.getMessage().getChat().getId());
            User user = new User();
            user.setId(userId);
            user.setName(update.getMessage().getFrom().getFirst_name());
            dbClient.saveUser(user);
            UserStatus userStatus = new UserStatus();
            userStatus.setUserId(user.getId());
            userStatus.setStatus("auth");
            dbClient.saveUserStatus(userStatus);
            return false;
        } else if (!(dbClient.isAuthDone(userId))) {
            String phoneNumber = update.getMessage().getContact().getPhone_number();
            User user = new User();
            user.setId(userId);
            user.setName(update.getMessage().getFrom().getFirst_name());
            user.setNumber(phoneNumber);
            dbClient.saveUser(user);
            ReplyMarkupDto deleteKeyBoard = new ReplyMarkupDto();
            deleteKeyBoard.setRemove_keyboard(true);
            MessageSendDto messageDeleteKeyBoard = new MessageSendDto(chatId, "Отлично твой номер сохранен");
            messageDeleteKeyBoard.setReply_markup(deleteKeyBoard);
            telegramClient.sendMessage(new MessageSendDto(chatId, "Отлично твой номер сохранен"));
            UserStatus userStatus = new UserStatus();
            userStatus.setUserId(user.getId());
            userStatus.setStatus("auth_done");
            dbClient.saveUserStatus(userStatus);
            return false;
        }
        return true;
    }
}
