package ru.tbank.knowhow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.tbank.knowhow.service.moder.ModerationService;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("${server.base-url.course}/moderation")
@RequiredArgsConstructor

public class ModerationController {
    private final ModerationService moderationService;

    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, String>> approveCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        moderationService.approveCourse(id, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Курс одобрен"));
    }

}
