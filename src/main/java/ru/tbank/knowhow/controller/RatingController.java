package ru.tbank.knowhow.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tbank.knowhow.service.rating.RatingService;

@Slf4j
@RestController
@RequestMapping("${server.base-url.course}")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/{courseId}/rating")
    public ResponseEntity<String> addRating(
            @PathVariable Long courseId,
            @RequestParam Integer grade,
            HttpServletRequest httpRequest) {

        Long userId = RequestAttributeExtractor.extractUserId(httpRequest);
        ratingService.insertRating(courseId, userId, grade);
        return ResponseEntity.ok().body("success");
    }
}
