package local.springdemobot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@Slf4j
public class SheduledGetUpdates {
    @Autowired
    private TelegramClient telegramClient;
    @Autowired
    private TelegramService telegramService;

    @Scheduled(fixedRate = 10)
    public void getUpdatesScheduling() {
        log.info("Sheduler");
        telegramService.processing(telegramClient.getUpdates());
    }
}