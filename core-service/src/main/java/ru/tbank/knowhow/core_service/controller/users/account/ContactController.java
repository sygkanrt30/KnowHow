package ru.tbank.knowhow.core_service.controller.users.account;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.knowhow.core_service.service.users.contact.UserContactService;
import ru.tbank.knowhow.core_service.util.RequestAttributeExtractor;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("${server.base-url.users}/contact")
public class ContactController {

    private final UserContactService userContactService;

    @PatchMapping("/email")
    public ResponseEntity<?> updateEmail(HttpServletRequest request,
            @RequestParam @Email String email
    ) {
        Long userId = RequestAttributeExtractor.extractUserId(request);
        userContactService.updateEmail(email, userId);
        return ResponseEntity.ok("Email updated successfully");
    }
}
