package local.springdemobot.service;

import local.springdemobot.enums.TypeUpdate;
import local.springdemobot.model.UpdateDto;
import lombok.NonNull;
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
        for (UpdateDto update : updates) {
            Long userId = update.getMessage().getFrom().getId();
            if (dbClient.isAuth(userId).isEmpty()) {
                this.userAuth(userId);
            }
            switch (update.getType()) {
                case TEXT:
                    log.info("Text");
                    break;
                case FILE:
                    log.info("File");
                    break;
                case OTHER:
                    log.info("Other");
                    break;
            }
            if (Objects.equals(update.getType(), TypeUpdate.TEXT)) {
                log.info("TEXT");
            }
        }
        if (updates.size() > 0) {
            Long lastOffset = updates.get(updates.size() - 1).getUpdate_id();
            offsetStore.setOffset(lastOffset + 1);
        }
    }

    private void userAuth(Long userId) {
        telegramClient.getNumber(userId);
        dbClient.userAuth(userId);
    }
}
