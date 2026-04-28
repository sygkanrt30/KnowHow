package ru.tbank.knowhow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.tbank.knowhow.service.moder.ModerationService;
import java.util.Map;

@RestController
@RequestMapping("${server.base-url.course}/moderation")
@RequiredArgsConstructor

public class ModerationController {
    private final ModerationService moderationService;

    @PostMapping("/{id}/approve")
    public ResponseEntity approveCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        moderationService.approveCourse(id, userDetails.getUsername());
        return ResponseEntity.ok("Курс одобрен");
    }

}
