package ru.tbank.knowhow.service.user;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.tbank.knowhow.ecxeption.RegistrationException;
import ru.tbank.knowhow.model.Balance;
import ru.tbank.knowhow.model.Role;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.model.dto.response.BalanceDto;
import ru.tbank.knowhow.model.dto.response.UsernameAndBalanceResponse;
import ru.tbank.knowhow.model.mapper.UsernameAndBalanceResponseMapper;
import ru.tbank.knowhow.repository.UserRepository;
import ru.tbank.knowhow.service.course.purchased.PurchasedCourseService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PurchasedCourseService purchasedCourseService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsernameAndBalanceResponseMapper usernameAndBalanceResponseMapper;

    private UserService userService;

    private static final String MODERATOR_CODE = "ADMIN123";
    private static final String USERNAME = "john_doe";
    private static final long START_COINS = 1000L;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                purchasedCourseService,
                passwordEncoder,
                usernameAndBalanceResponseMapper,
                MODERATOR_CODE,
                START_COINS
        );
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenUserExists() {
        User expectedUser = User.builder()
                .id(1L)
                .username(USERNAME)
                .build();
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(expectedUser));

        Optional<User> result = userService.findByUsername(USERNAME);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(USERNAME);
        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenUserDoesNotExist() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername(USERNAME);

        assertThat(result).isEmpty();
        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void getByUsernameOrElseThrow_ShouldReturnUser_WhenUserExists() {
        User expectedUser = User.builder()
                .id(1L)
                .username(USERNAME)
                .build();
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(expectedUser));

        User result = userService.getByUsernameOrElseThrow(USERNAME);

        assertThat(result.getUsername()).isEqualTo(USERNAME);
        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void getByUsernameOrElseThrow_ShouldThrowUsernameNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByUsernameOrElseThrow(USERNAME))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User with username %s not found", USERNAME);
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        Long userId = 1L;
        User expectedUser = User.builder().id(userId).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        Optional<User> result = userService.findById(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(userId);
        verify(userRepository).findById(userId);
    }

    @Test
    void getByIdOrElseThrow_ShouldReturnUser_WhenUserExists() {
        Long userId = 1L;
        User expectedUser = User.builder().id(userId).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        User result = userService.getByIdOrElseThrow(userId);

        assertThat(result.getId()).isEqualTo(userId);
        verify(userRepository).findById(userId);
    }

    @Test
    void getByIdOrElseThrow_ShouldThrowEntityNotFoundException_WhenUserDoesNotExist() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByIdOrElseThrow(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found by id: " + userId);
    }

    @Test
    void getCurrentUser_ShouldReturnUsernameAndBalanceResponse_WhenUserExists() {
        Long userId = 1L;
        var balance = new Balance(1L, START_COINS);
        var user = User.builder()
                .id(userId)
                .username("john_doe")
                .role(Role.USER)
                .balance(balance)
                .build();
        var balanceDto = new BalanceDto(balance.getId(), userId, START_COINS);
        var expectedResponse = new UsernameAndBalanceResponse(user.getUsername(), user.getRole(), balanceDto);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(usernameAndBalanceResponseMapper.toUsernameAndBalanceResponse(user)).thenReturn(expectedResponse);

        UsernameAndBalanceResponse result = userService.getCurrentUser(userId);

        assertThat(result).isEqualTo(expectedResponse);
        verify(userRepository).findById(userId);
        verify(usernameAndBalanceResponseMapper).toUsernameAndBalanceResponse(user);
    }

    @Test
    void getCurrentUser_ShouldThrowEntityNotFoundException_WhenUserDoesNotExist() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with id: " + userId);

        verify(userRepository).findById(userId);
        verifyNoInteractions(usernameAndBalanceResponseMapper);
    }

    @Test
    void save_ShouldCreateUserWithRoleUser_WhenModeratorCodeIsNull() {
        byte[] password = "password123".getBytes();
        String email = "john@example.com";
        when(passwordEncoder.encode(any(String.class))).thenReturn("encoded_password");

        userService.save(USERNAME, password, email, null);

        verify(userRepository).saveAndFlush(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void save_ShouldCreateUserWithRoleUser_WhenModeratorCodeIsInvalid() {
        byte[] password = "password123".getBytes();
        String email = "john@example.com";
        String moderatorCode = "WRONG_CODE";

        when(passwordEncoder.encode(any(String.class))).thenReturn("encoded_password");

        userService.save(USERNAME, password, email, moderatorCode);

        verify(userRepository).saveAndFlush(argThat(user ->
                user.getRole() == Role.USER &&
                        user.getUsername().equals(USERNAME) &&
                        user.getEmail().equals(email) &&
                        user.getBalance().getCoins() == START_COINS
        ));
    }

    @Test
    void save_ShouldCreateUserWithRoleModerator_WhenModeratorCodeIsValid() {
        byte[] password = "password123".getBytes();
        String email = "moderator@example.com";
        when(passwordEncoder.encode(any(String.class))).thenReturn("encoded_password");

        userService.save(USERNAME, password, email, MODERATOR_CODE);

        verify(userRepository).saveAndFlush(argThat(user -> user.getRole() == Role.MODERATOR));
    }

    @Test
    void save_ShouldThrowRegistrationException_WhenRepositoryFails() {
        byte[] password = "password123".getBytes();
        String email = "john@example.com";
        when(passwordEncoder.encode(any(String.class))).thenReturn("encoded_password");
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> userService.save(USERNAME, password, email, null))
                .isInstanceOf(RegistrationException.class)
                .hasMessageContaining("Database error");

        verify(userRepository).saveAndFlush(any(User.class));
    }

    @Test
    void deleteById_ShouldDeleteUserAndPurchasedCourses_WhenUserExists() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteById(userId);

        verify(userRepository).deleteById(userId);
        verify(purchasedCourseService).deleteAllPurchasedCoursesByUserId(userId);
    }

    @Test
    void deleteById_ShouldThrowEntityNotFoundException_WhenUserDoesNotExist() {
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteById(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found by id: " + userId);

        verify(userRepository, never()).deleteById(any());
        verify(purchasedCourseService, never()).deleteAllPurchasedCoursesByUserId(any());
    }
}