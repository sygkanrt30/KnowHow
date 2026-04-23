package ru.tbank.knowhow.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.knowhow.model.dto.response.ProfileDto;
import ru.tbank.knowhow.service.profile.GetProfileService;

@RequiredArgsConstructor
@RestController
@RequestMapping("${server.base-url.profile}")
public class ProfileController {

    private final GetProfileService getProfileService;

    @GetMapping
    public ResponseEntity<ProfileDto> getProfile(HttpServletRequest request) {
        Long userId = RequestAttributeExtractor.extractUserId(request);
        return ResponseEntity.ok(getProfileService.getProfile(userId));
    }
}
