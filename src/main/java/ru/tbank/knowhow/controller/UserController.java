package ru.tbank.knowhow.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.knowhow.service.user.DeleteUserService;

@RestController
@RequestMapping("${server.base-url.users}")
@RequiredArgsConstructor
@Slf4j
class UserController {

    private final DeleteUserService deleteUserService;

    @DeleteMapping()
    public ResponseEntity<?> deleteUserById(HttpServletRequest request) {
        Long userId = RequestAttributeExtractor.extractUserId(request);
        deleteUserService.deleteById(userId);
        log.info("Deleted account for user id={}", userId);
        return ResponseEntity.ok("Deleted account for user id=" + userId);
    }
}
