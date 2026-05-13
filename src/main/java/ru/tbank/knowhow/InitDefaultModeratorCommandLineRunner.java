package ru.tbank.knowhow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.tbank.knowhow.service.user.UserService;

@Component
@Slf4j
public class InitDefaultModeratorCommandLineRunner implements CommandLineRunner {

    private final UserService userService;
    private final String moderatorCode;

    public InitDefaultModeratorCommandLineRunner(UserService userService,
                                                 @Value("${moderator.code}") String moderatorCode) {
        this.userService = userService;
        this.moderatorCode = moderatorCode;
    }

    @Override
    public void run(String... args) {
        if (userService.findByUsername("moderator123").isEmpty()) {
            userService.save(
                    "moderator123",
                    "moderator123".getBytes(),
                    "moderator.123@gmail.com",
                    moderatorCode
            );
            log.info("Default user created via CommandLineRunner");
        }
    }
}
