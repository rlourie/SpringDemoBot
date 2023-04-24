package local.springdemobot.service;

import local.springdemobot.database.entites.User;
import local.springdemobot.database.repository.UserRepository;
import local.springdemobot.database.repository.UserStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DbClient {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserStatusRepository userStatusRepository;

    public Optional<User> isAuth(Long userId) {
        return userRepository.findById(userId);
    }

    public void userAuth(Long userId) {
    }
}
