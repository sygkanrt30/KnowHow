package ru.tbank.knowhow.controller.users.auth;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import ru.tbank.knowhow.ecxeption.LoginException;
import ru.tbank.knowhow.ecxeption.RegistrationException;
import ru.tbank.knowhow.model.users.Role;
import ru.tbank.knowhow.model.dto.user.balance.request.BalanceDto;
import ru.tbank.knowhow.model.dto.user.response.UsernameAndBalanceResponse;
import ru.tbank.knowhow.security.AttributeName;
import ru.tbank.knowhow.service.users.GetUserService;
import ru.tbank.knowhow.service.users.SaveUserService;
import ru.tbank.knowhow.util.Authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Instancio.of;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Tag("integration-controller")
class AuthControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private SaveUserService saveUserService;

    @MockitoBean
    private GetUserService getUserService;

    @MockitoBean
    private Authenticator authenticator;

    @Value("${server.base-url.auth}")
    private String url;

    @Test
    @DisplayName("doReg should return 200 when registration is successful")
    void shouldReturn200WhenRegistrationSuccessful() {
        String username = "ValidUser123";
        String password = "ValidPass123!";
        String email = "test@example.com";

        doNothing().when(saveUserService).save(username, password.getBytes(), email, null);
        doNothing().when(authenticator).authenticateAndSetCookie(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class),
                eq(username),
                eq(password.getBytes())
        );

        assertThat(mockMvc.post()
                .uri(url + "/reg")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "%s",
                            "password": "%s",
                            "email": "%s"
                        }
                        """.formatted(username, password, email))
                .with(csrf()))
                .hasStatusOk()
                .body()
                .asString()
                .isEqualTo("Sign up successful");

        verify(saveUserService, times(1)).save(username, password.getBytes(), email, null);
        verify(authenticator, times(1)).authenticateAndSetCookie(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class),
                eq(username),
                eq(password.getBytes())
        );
    }

    @Test
    @DisplayName("doReg should return 200 when registration with moderator code is successful")
    void shouldReturn200WhenRegistrationWithModeratorCodeSuccessful() {
        String username = "ModeratorUser";
        String password = "ValidPass123!";
        String email = "moderator@example.com";
        String moderatorCode = "SECRET123";

        doNothing().when(saveUserService).save(username, password.getBytes(), email, moderatorCode);
        doNothing().when(authenticator).authenticateAndSetCookie(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class),
                eq(username),
                eq(password.getBytes())
        );

        assertThat(mockMvc.post()
                .uri(url + "/reg")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "%s",
                            "password": "%s",
                            "email": "%s",
                            "moderatorCode": "%s"
                        }
                        """.formatted(username, password, email, moderatorCode))
                .with(csrf()))
                .hasStatusOk();

        verify(saveUserService, times(1)).save(username, password.getBytes(), email, moderatorCode);
    }

    @Test
    @DisplayName("doReg should return 400 when username is invalid")
    void shouldReturn400WhenUsernameInvalid() {
        String invalidUsername = "invalid username with spaces";
        String password = "ValidPass123!";
        String email = "test@example.com";

        assertThat(mockMvc.post()
                .uri(url + "/reg")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "%s",
                            "password": "%s",
                            "email": "%s"
                        }
                        """.formatted(invalidUsername, password, email))
                .with(csrf()))
                .hasStatus(HttpStatus.BAD_REQUEST);

        verify(saveUserService, never()).save(any(), any(), any(), any());
        verify(authenticator, never()).authenticateAndSetCookie(any(), any(), any(), any());
    }

    @Test
    @DisplayName("doReg should return 400 when username is blank")
    void shouldReturn400WhenUsernameBlank() {
        String username = "";
        String password = "ValidPass123!";
        String email = "test@example.com";

        assertThat(mockMvc.post()
                .uri(url + "/reg")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "%s",
                            "password": "%s",
                            "email": "%s"
                        }
                        """.formatted(username, password, email))
                .with(csrf()))
                .hasStatus(HttpStatus.BAD_REQUEST);

        verify(saveUserService, never()).save(any(), any(), any(), any());
    }

    @Test
    @DisplayName("doReg should return 400 when password is invalid (no uppercase)")
    void shouldReturn400WhenPasswordNoUppercase() {
        String username = "ValidUser";
        String invalidPassword = "validpass123!";
        String email = "test@example.com";

        assertThat(mockMvc.post()
                .uri(url + "/reg")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "%s",
                            "password": "%s",
                            "email": "%s"
                        }
                        """.formatted(username, invalidPassword, email))
                .with(csrf()))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("doReg should return 400 when password is invalid (no special char)")
    void shouldReturn400WhenPasswordNoSpecialChar() {
        String username = "ValidUser";
        String invalidPassword = "ValidPass123";
        String email = "test@example.com";

        assertThat(mockMvc.post()
                .uri(url + "/reg")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "%s",
                            "password": "%s",
                            "email": "%s"
                        }
                        """.formatted(username, invalidPassword, email))
                .with(csrf()))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("doReg should return 400 when email is invalid")
    void shouldReturn400WhenEmailInvalid() {
        String username = "ValidUser";
        String password = "ValidPass123!";
        String invalidEmail = "invalid-email";

        assertThat(mockMvc.post()
                .uri(url + "/reg")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "%s",
                            "password": "%s",
                            "email": "%s"
                        }
                        """.formatted(username, password, invalidEmail))
                .with(csrf()))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("doReg should return 409 when user already exists")
    void shouldReturn409WhenUserAlreadyExists() {
        String username = "ExistingUser";
        String password = "ValidPass123!";
        String email = "existing@example.com";

        doThrow(new RegistrationException("User already exists with username: " + username))
                .when(saveUserService).save(username, password.getBytes(), email, null);

        assertThat(mockMvc.post()
                .uri(url + "/reg")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "%s",
                            "password": "%s",
                            "email": "%s"
                        }
                        """.formatted(username, password, email))
                .with(csrf()))
                .hasStatus(HttpStatus.CONFLICT);

        verify(saveUserService, times(1)).save(username, password.getBytes(), email, null);
        verify(authenticator, never()).authenticateAndSetCookie(any(), any(), any(), any());
    }

    @Test
    @DisplayName("doReg should return 409 when authentication fails after registration")
    void shouldReturn409WhenAuthenticationFails() {
        String username = "ValidUser";
        String password = "ValidPass123!";
        String email = "test@example.com";

        doNothing().when(saveUserService).save(username, password.getBytes(), email, null);
        doThrow(new RegistrationException("Authentication failed after registration"))
                .when(authenticator).authenticateAndSetCookie(
                        any(HttpServletRequest.class),
                        any(HttpServletResponse.class),
                        eq(username),
                        eq(password.getBytes())
                );

        assertThat(mockMvc.post()
                .uri(url + "/reg")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "%s",
                            "password": "%s",
                            "email": "%s"
                        }
                        """.formatted(username, password, email))
                .with(csrf()))
                .hasStatus(HttpStatus.CONFLICT);

        verify(saveUserService, times(1)).save(username, password.getBytes(), email, null);
        verify(authenticator, times(1)).authenticateAndSetCookie(any(), any(), any(), any());
    }

    @Test
    @DisplayName("login should return 200 when login is successful")
    void shouldReturn200WhenLoginSuccessful() {
        String username = "ValidUser123";
        String password = "ValidPass123!";

        doNothing().when(authenticator).authenticateAndSetCookie(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class),
                eq(username),
                eq(password.getBytes())
        );

        assertThat(mockMvc.post()
                .uri(url + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "%s",
                            "password": "%s"
                        }
                        """.formatted(username, password))
                .with(csrf()))
                .hasStatusOk()
                .body()
                .asString()
                .isEqualTo("Log in successful");

        verify(authenticator, times(1)).authenticateAndSetCookie(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class),
                eq(username),
                eq(password.getBytes())
        );
    }

    @Test
    @DisplayName("login should return 400 when username is invalid")
    void shouldReturn400WhenLoginUsernameInvalid() {
        String invalidUsername = "invalid username";
        String password = "ValidPass123!";

        assertThat(mockMvc.post()
                .uri(url + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "%s",
                            "password": "%s"
                        }
                        """.formatted(invalidUsername, password))
                .with(csrf()))
                .hasStatus(HttpStatus.BAD_REQUEST);

        verify(authenticator, never()).authenticateAndSetCookie(any(), any(), any(), any());
    }

    @Test
    @DisplayName("login should return 400 when password is invalid")
    void shouldReturn400WhenLoginPasswordInvalid() {
        String username = "ValidUser";
        String invalidPassword = "weak";

        assertThat(mockMvc.post()
                .uri(url + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "%s",
                            "password": "%s"
                        }
                        """.formatted(username, invalidPassword))
                .with(csrf()))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("login should return 401 when credentials are invalid")
    void shouldReturn401WhenCredentialsInvalid() {
        String username = "WrongUser";
        String password = "WrongPass123!";

        doThrow(new LoginException("Authentication failed"))
                .when(authenticator).authenticateAndSetCookie(
                        any(HttpServletRequest.class),
                        any(HttpServletResponse.class),
                        eq(username),
                        eq(password.getBytes())
                );

        assertThat(mockMvc.post()
                .uri(url + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "%s",
                            "password": "%s"
                        }
                        """.formatted(username, password))
                .with(csrf()))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @WithMockUser
    @DisplayName("getCurrentUser should return 200 with current user data")
    void shouldReturnCurrentUser() {
        Long userId = 1L;

        BalanceDto balanceDto = of(BalanceDto.class)
                .set(field(BalanceDto::id), 1L)
                .set(field(BalanceDto::userId), userId)
                .set(field(BalanceDto::coins), 1000L)
                .create();

        UsernameAndBalanceResponse expectedResponse = of(UsernameAndBalanceResponse.class)
                .set(field(UsernameAndBalanceResponse::username), "john_doe")
                .set(field(UsernameAndBalanceResponse::role), Role.USER)
                .set(field(UsernameAndBalanceResponse::balance), balanceDto)
                .create();

        when(getUserService.getCurrentUser(userId)).thenReturn(expectedResponse);

        assertThat(mockMvc.get()
                .uri(url + "/me")
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), userId))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.username").isEqualTo("john_doe");
                    assertThat(json).extractingPath("$.role").isEqualTo("USER");
                    assertThat(json).extractingPath("$.balance.userId").isEqualTo(userId.intValue());
                    assertThat(json).extractingPath("$.balance.coins").isEqualTo(1000);
                });

        verify(getUserService, times(1)).getCurrentUser(userId);
    }

    @Test
    @WithMockUser
    @DisplayName("getCurrentUser should return 404 when user not found")
    void shouldReturn404WhenCurrentUserNotFound() {
        Long userId = 999L;

        when(getUserService.getCurrentUser(userId))
                .thenThrow(new EntityNotFoundException("User not found with id: " + userId));

        assertThat(mockMvc.get()
                .uri(url + "/me")
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), userId))
                .hasStatus(HttpStatus.NOT_FOUND);

        verify(getUserService, times(1)).getCurrentUser(userId);
    }
}