package ru.tbank.knowhow.core_service.controller.users.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.tbank.knowhow.core_service.model.dto.auth.request.UserCredentialsForAuth;
import ru.tbank.knowhow.core_service.model.dto.auth.request.UserCredentialsForReg;
import ru.tbank.knowhow.core_service.model.dto.user.response.UsernameAndBalanceResponse;
import ru.tbank.knowhow.core_service.service.users.GetUserService;
import ru.tbank.knowhow.core_service.service.users.SaveUserService;
import ru.tbank.knowhow.core_service.util.Authenticator;
import ru.tbank.knowhow.core_service.util.RequestAttributeExtractor;


@RestController
@RequestMapping("${server.base-url.auth}")
@RequiredArgsConstructor
@Validated
@Slf4j
class AuthController {

    private final SaveUserService userService;
    private final GetUserService getUserService;
    private final Authenticator authenticator;

    @PostMapping("/reg")
    public ResponseEntity<String> doReg(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody @Valid UserCredentialsForReg userCredentials) {
        var username = userCredentials.username();
        var password = userCredentials.password().getBytes();
        userService.save(username, password, userCredentials.email(), userCredentials.moderatorCode());
        authenticator.authenticateAndSetCookie(request, response, username, password);
        return ResponseEntity.ok("Sign up successful");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody @Valid UserCredentialsForAuth userCredentials) {
        log.info("Login attempt for user: {}", userCredentials.username());
        authenticator.authenticateAndSetCookie(request, response, userCredentials.username(),
                userCredentials.password().getBytes()
        );
        return ResponseEntity.ok("Log in successful");
    }

    @GetMapping("/me")
    public ResponseEntity<UsernameAndBalanceResponse> getCurrentUser(HttpServletRequest request) {
        Long id = RequestAttributeExtractor.extractUserId(request);
        return ResponseEntity.ok(getUserService.getCurrentUser(id));
    }
}
