package local.springdemobot.loggining;

import local.springdemobot.database.repository.UserRepository;
import local.springdemobot.database.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty("db-show.enabled")
public class LogerDb implements ApplicationRunner {
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Runner");
        log.info("Data from user: {}", userRepository.findAll());
        log.info("Data from user status: {}", userStatusRepository.findAll());
    }
}
