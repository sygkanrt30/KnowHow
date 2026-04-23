package ru.tbank.knowhow.service.user;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.ecxeption.RegistrationException;
import ru.tbank.knowhow.model.Balance;
import ru.tbank.knowhow.model.Role;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.model.dto.response.UsernameAndBalanceResponse;
import ru.tbank.knowhow.model.mapper.UsernameAndBalanceResponseMapper;
import ru.tbank.knowhow.repository.CourseRepository;
import ru.tbank.knowhow.repository.UserRepository;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class UserService implements GetUserInfoService, SaveUserService, DeleteUserService {

    private final long startCoins;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsernameAndBalanceResponseMapper usernameAndBalanceResponseMapper;
    private final String moderatorCode;

    @Autowired
    public UserService(UserRepository userRepository, CourseRepository courseRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${moderator.code}") String moderatorCode,
                       UsernameAndBalanceResponseMapper usernameAndBalanceResponseMapper,
                       @Value("${coins.start-amount}") long startCoins) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.passwordEncoder = passwordEncoder;
        this.moderatorCode = moderatorCode;
        this.startCoins = startCoins;
        this.usernameAndBalanceResponseMapper = usernameAndBalanceResponseMapper;
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
    public UsernameAndBalanceResponse getCurrentUser(Long id) {
        User user = this.findById(id).orElseThrow(
                () -> new EntityNotFoundException("User not found with id: " + id)
        );
        return usernameAndBalanceResponseMapper.toUsernameAndBalanceResponse(user);
    }

    @Override
    @Transactional
    public void save(String username, byte[] password, String email, String moderatorCode) {
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

    private Role getRole(String moderatorCode) {
        if (Objects.nonNull(moderatorCode) && this.moderatorCode.equals(moderatorCode)) {
            return Role.MODERATOR;
        }
        return Role.USER;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found by id: " + id);
        }
        if (courseRepository.existsByAuthorId(id)) {
            throw new IllegalStateException("Cannot delete user with authored courses");
        }
        userRepository.deleteById(id);
    }
}
