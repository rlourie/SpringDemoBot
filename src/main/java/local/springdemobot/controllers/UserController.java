package local.springdemobot.controllers;

import local.springdemobot.database.repository.UserRepository;
import local.springdemobot.model.UpdateDto;
import local.springdemobot.service.TelegramClient;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/1")
@AllArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private TelegramClient telegramClient;

    @GetMapping()
    public ResponseEntity<Object> getAllUsers(@RequestParam(required = false) Long id) {
        if (id == null) {
            return ResponseEntity.ok(null);
        }
        return ResponseEntity.ok(this.userRepository.findById(id));
    }

    @GetMapping("/2")
    public ResponseEntity<Object> getUpdates() {
        List<UpdateDto> updates = telegramClient.getUpdates();
        return ResponseEntity.ok(updates);
    }

}
