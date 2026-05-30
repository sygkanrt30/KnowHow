package ru.tbank.knowhow.core_service.controller.rating;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tbank.knowhow.core_service.service.rating.RatingService;
import ru.tbank.knowhow.core_service.util.RequestAttributeExtractor;

@Slf4j
@RestController
@RequestMapping("${server.base-url.course}")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/{courseId}/rating")
    public ResponseEntity<String> insertRating(
            @PathVariable Long courseId,
            @RequestParam Integer grade,
            HttpServletRequest httpRequest) {

        Long userId = RequestAttributeExtractor.extractUserId(httpRequest);
        ratingService.insertRating(courseId, userId, grade);
        return ResponseEntity.ok().body("success");
    }
}
