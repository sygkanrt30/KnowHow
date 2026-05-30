package ru.tbank.knowhow.core_service.controller.users.account;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.tbank.knowhow.core_service.model.users.NotificationContact;
import ru.tbank.knowhow.core_service.service.users.contact.UserContactService;
import ru.tbank.knowhow.core_service.util.RequestAttributeExtractor;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("${server.base-url.users}/contact")
public class ContactController {

    private final UserContactService userContactService;

    @PostMapping("/tg")
    public ResponseEntity<?> addOrUpdateTgUsername(HttpServletRequest request,
            @RequestParam @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{4,31}$") String tgUsername
    ) {
        Long userId = RequestAttributeExtractor.extractUserId(request);
        userContactService.insertTgUsername(tgUsername, userId);
        return ResponseEntity.ok("Tg username insert successfully");
    }

    @PatchMapping("/email")
    public ResponseEntity<?> updateEmail(HttpServletRequest request,
            @RequestParam @Email String email
    ) {
        Long userId = RequestAttributeExtractor.extractUserId(request);
        userContactService.updateEmail(email, userId);
        return ResponseEntity.ok("Email updated successfully");
    }

    @GetMapping("/tg")
    public ResponseEntity<String> getTgUsername(HttpServletRequest request) {
        Long userId = RequestAttributeExtractor.extractUserId(request);
        return ResponseEntity.ok(userContactService.getTgUsername(userId));
    }

    @GetMapping("/primary_notification")
    public ResponseEntity<NotificationContact> getPrimaryNotificationContact(HttpServletRequest request) {
        Long userId = RequestAttributeExtractor.extractUserId(request);
        return ResponseEntity.ok(userContactService.getPrimaryNotificationContact(userId));
    }

    @PatchMapping("/primary_notification")
    public ResponseEntity<NotificationContact> updatePrimaryNotificationContact(
            HttpServletRequest request,
            @RequestParam String notificationContact) {
        Long userId = RequestAttributeExtractor.extractUserId(request);
        NotificationContact enumNotificationContact = NotificationContact.fromString(notificationContact);
        return ResponseEntity.ok(userContactService.changePrimaryNotificationContact(userId, enumNotificationContact));
    }
}
