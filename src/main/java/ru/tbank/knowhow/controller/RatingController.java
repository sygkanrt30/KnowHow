package ru.tbank.knowhow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.tbank.knowhow.service.rating.RatingService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("${server.base-url.course}")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/{id}/rating")
    public ResponseEntity<Map<String, String>> addRating(
            @PathVariable Long id,
            @RequestParam Integer grade,
            @AuthenticationPrincipal UserDetails userDetails) {

        boolean success = ratingService.addRating(id, grade, userDetails.getUsername());

        if (success) {
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            log.warn("User {} already rated course {}", userDetails.getUsername(), id);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("message", "Вы уже оценили этот курс"));
        }
    }
}
