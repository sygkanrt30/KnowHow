package ru.tbank.knowhow.controller.moderation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.tbank.knowhow.model.dto.moderation.request.ModerationRejectRequest;
import ru.tbank.knowhow.model.dto.course.response.CourseDto;
import ru.tbank.knowhow.service.moderation.verdict.CourseVerdictService;
import ru.tbank.knowhow.service.moderation.ModerationService;
import ru.tbank.knowhow.util.RequestAttributeExtractor;

import java.util.List;

@Validated
@RestController
@RequestMapping("${server.base-url.course}/moderation")
@RequiredArgsConstructor

public class ModerationController {

    private final CourseVerdictService courseVerdictService;
    private final ModerationService moderationService;

    @PostMapping("/{id}/approve")
    public ResponseEntity<String> approveCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        courseVerdictService.approveCourse(id, userDetails.getUsername());
        return ResponseEntity.ok("Course approved");
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<String> rejectCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ModerationRejectRequest request) {

        courseVerdictService.rejectCourse(id, userDetails.getUsername(), request.reason());
        return ResponseEntity.ok("Course rejected");
    }

    @GetMapping("/queue/on_moderation")
    public ResponseEntity<List<CourseDto>> onModeration(HttpServletRequest request) {
        Long moderationId = RequestAttributeExtractor.extractUserId(request);
        return ResponseEntity.ok(moderationService.findAllCoursesOnModerationByModeratorId(moderationId));
    }
}
