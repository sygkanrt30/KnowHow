package ru.tbank.knowhow.service.user;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.tbank.knowhow.ecxeption.RegistrationException;
import ru.tbank.knowhow.model.Balance;
import ru.tbank.knowhow.model.Role;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.repository.UserRepository;

import java.util.Optional;

@Service
@Slf4j
public class UserService implements GetUserInfoService, SaveUserService {

    private final long startCoins;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String moderatorCode;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       @Value("${moderator.code}") String moderatorCode,
                       @Value("${coins.start-amount}") long startCoins) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.moderatorCode = moderatorCode;
        this.startCoins = startCoins;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional
    public void save(String username, byte[] password, String email, byte[] moderatorCode) {
        Role role = getRole(moderatorCode);
        try {
            var balance = new Balance(startCoins);
            var user = User.builder()
                    .username(username)
                    .email(email)
                    .role(role)
                    .password(passwordEncoder.encode(new String(password)))
                    .balance(balance)
                    .build();
            userRepository.saveAndFlush(user);
            log.info("Saved user: {}", user);
        } catch (Exception e) {
            throw new RegistrationException(e.getMessage(), e);
        }
    }

    private Role getRole(byte[] moderatorCode) {
        if (this.moderatorCode.equals(new String(moderatorCode))) {
            return Role.MODERATOR;
        }
        return Role.USER;
    }
}
