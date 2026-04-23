package ru.tbank.knowhow.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tbank.knowhow.model.dto.response.UserResponse;
import ru.tbank.knowhow.service.user.GetUserInfoService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
class UserController {

    private final GetUserInfoService getUserInfoService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        var user = getUserInfoService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found by id: " + id));

        var response = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getBalance().getCoins()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = RequestAttributeExtractor.extractUserId(request);
        if (!id.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can delete only your own account");
        }
        getUserInfoService.deleteById(id);
        log.info("Deleted account for user id={}", id);
        return ResponseEntity.ok().build();
    }
}
