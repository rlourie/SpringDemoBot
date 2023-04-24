package local.springdemobot.service;

import local.springdemobot.model.OffsetStore;
import local.springdemobot.model.UpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class TelegramService {
    @Autowired
    private OffsetStore offsetStore;

    public void processing(List<UpdateDto> updates) {
        for (UpdateDto update : updates) {
            log.info(update.getMessage().getText());
        }
        Long lastOffset = updates.get(updates.size() - 1).getUpdate_id();
        offsetStore.setOffset(lastOffset + 1);
    }
}
