package ru.tbank.knowhow.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.tbank.knowhow.model.dto.request.UserCredentialsForAuth;
import ru.tbank.knowhow.model.dto.request.UserCredentialsForReg;
import ru.tbank.knowhow.service.user.SaveUserService;


@RestController
@RequestMapping("${server.base-url.auth}")
@RequiredArgsConstructor
@Validated
@Slf4j
class AuthController {

    private final SaveUserService userService;
    private final Authenticator authenticator;

    @PostMapping("/reg")
    public ResponseEntity<String> doReg(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody @Valid UserCredentialsForReg userCredentials) {
        var username = userCredentials.username();
        var password = userCredentials.password().getBytes();
        userService.save(username, password, userCredentials.email(), userCredentials.moderatorCode().getBytes());
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
}
