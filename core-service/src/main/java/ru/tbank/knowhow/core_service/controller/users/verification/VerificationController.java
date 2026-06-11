package ru.tbank.knowhow.core_service.controller.users.verification;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tbank.shared.events.NotificationContactType;
import ru.tbank.knowhow.core_service.service.verification.VerificationService;
import ru.tbank.knowhow.core_service.util.RequestAttributeExtractor;

@RestController
@RequestMapping("${server.base-url.users}/contact/verification")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@RequestParam String contact, HttpServletRequest request) {
        Long userId = RequestAttributeExtractor.extractUserId(request);
        NotificationContactType contactEnum = NotificationContactType.fromString(contact);
        verificationService.generateAndSendCode(contactEnum, userId);
        return ResponseEntity.ok("Code sent");
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(HttpServletRequest request,
                                         @RequestParam String contact,
                                         @RequestParam String code) {

        Long userId = RequestAttributeExtractor.extractUserId(request);
        NotificationContactType contactEnum = NotificationContactType.fromString(contact);
        boolean isVerified = verificationService.verifyContact(contactEnum, code, userId);
        if (isVerified) {
            return ResponseEntity.ok("Email confirmed!");
        } else {
            return ResponseEntity.badRequest().body("Invalid code");
        }
    }
}
