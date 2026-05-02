package ru.tbank.knowhow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.tbank.knowhow.model.dto.request.ModerationRejectRequest;
import ru.tbank.knowhow.service.moder.ModerationService;
import java.util.Map;

@Validated
@RestController
@RequestMapping("${server.base-url.course}/moderation")
@RequiredArgsConstructor

public class ModerationController {
    private final ModerationService moderationService;

    @PostMapping("/{id}/approve")
    public ResponseEntity<String> approveCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        moderationService.approveCourse(id, userDetails.getUsername());
        return ResponseEntity.ok("Курс одобрен");
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<String> rejectCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ModerationRejectRequest request) {

        moderationService.rejectCourse(id, userDetails.getUsername(), request.getReason());
        return ResponseEntity.ok("Курс отклонён");
    }
}
