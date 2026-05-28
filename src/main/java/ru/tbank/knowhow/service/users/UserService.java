package ru.tbank.knowhow.service.users;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.ecxeption.RegistrationException;
import ru.tbank.knowhow.model.users.balance.Balance;
import ru.tbank.knowhow.model.users.Role;
import ru.tbank.knowhow.model.users.User;
import ru.tbank.knowhow.model.users.UserContact;
import ru.tbank.knowhow.model.dto.user.response.UserProjectionForProfile;
import ru.tbank.knowhow.model.dto.user.response.UsernameAndBalanceResponse;
import ru.tbank.knowhow.mappers.UsernameAndBalanceResponseMapper;
import ru.tbank.knowhow.repository.UserRepository;
import ru.tbank.knowhow.service.purchased.PurchasedCourseService;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class UserService implements GetUserService, SaveUserService, DeleteUserService {

    private final UserRepository userRepository;
    private final PurchasedCourseService purchasedCourseService;
    private final PasswordEncoder passwordEncoder;
    private final UsernameAndBalanceResponseMapper usernameAndBalanceResponseMapper;
    private final String moderatorCode;
    private final long startCoins;

    @Autowired
    public UserService(UserRepository userRepository,
                       PurchasedCourseService purchasedCourseService,
                       PasswordEncoder passwordEncoder,
                       UsernameAndBalanceResponseMapper usernameAndBalanceResponseMapper,
                       @Value("${moderator.code}") String moderatorCode,
                       @Value("${coins.start-amount}") long startCoins) {

        this.userRepository = userRepository;
        this.purchasedCourseService = purchasedCourseService;
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
    public User getByUsernameOrElseThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username %s not found".formatted(username)));
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User getByIdOrElseThrow(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("User not found by id: " + id));
    }

    @Override
    public Optional<UserProjectionForProfile> getProjectionForProfile(Long id) {
        return userRepository.getProjectionById(id);
    }

    @Override
    @Transactional
    public UsernameAndBalanceResponse getCurrentUser(Long id) {
        User user = getByIdOrElseThrow(id);
        return usernameAndBalanceResponseMapper.toUsernameAndBalanceResponse(user);
    }

    @Override
    @Transactional
    public void save(String username, byte[] password, String email, String moderatorCode) {
        try {
            UserContact userContact = new UserContact(email);
            var user = User.builder()
                    .username(username)
                    .userContact(userContact)
                    .password(passwordEncoder.encode(new String(password)))
                    .build();
            setNotCommonAttribute(user, moderatorCode);
            userRepository.saveAndFlush(user);
            log.info("Saved user: {}", user);
        } catch (Exception e) {
            throw new RegistrationException(e.getMessage(), e);
        }
    }

    private void setNotCommonAttribute(User user, String moderatorCode) {
        if (!(Objects.nonNull(moderatorCode) && this.moderatorCode.equals(moderatorCode))) {
            user.setRole(Role.USER);
            user.setBalance(new Balance(startCoins));
            return;
        }
        user.setRole(Role.MODERATOR);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found by id: " + id);
        }
        userRepository.deleteById(id);
        purchasedCourseService.deleteAllPurchasedCoursesByUserId(id);
        log.info("Deleted account for user id={}", id);
    }
}
