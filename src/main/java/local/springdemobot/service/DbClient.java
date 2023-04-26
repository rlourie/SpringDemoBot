package local.springdemobot.service;

import local.springdemobot.database.entites.User;
import local.springdemobot.database.entites.UserStatus;
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

    public Optional<User> findUserById(Long userId) {
        return userRepository.findById(userId);
    }
    public Boolean isAuthDone(Long userId) {
        Optional<UserStatus> userStatus = Optional.ofNullable(userStatusRepository.findAllByIdAndStatusAuthDone(userId));
        return userStatus.isPresent();

    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public void saveUserStatus(UserStatus userStatus) {
        userStatusRepository.save(userStatus);
    }

}
