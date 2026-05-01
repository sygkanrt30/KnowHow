package ru.tbank.knowhow.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.tbank.knowhow.model.dto.request.SortRequest;
import ru.tbank.knowhow.model.dto.response.CourseDto;
import ru.tbank.knowhow.service.course.GetCourseService;
import ru.tbank.knowhow.service.user.DeleteUserService;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@RequestMapping("${server.base-url.users}")
public class AccountController {

    private final GetCourseService getCourseService;
    private final DeleteUserService deleteUserService;

    @GetMapping("/purchased-courses")
    public ResponseEntity<Page<CourseDto>> getPurchasedCourses(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestBody @Valid SortRequest sortRequest
    ) {
        Long userId = RequestAttributeExtractor.extractUserId(request);
        Page<CourseDto> purchasedCourses = getCourseService.findAllPurchasedCourses(userId, page, size, sortRequest);
        return ResponseEntity.ok(purchasedCourses);
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteUserById(HttpServletRequest request) {
        Long userId = RequestAttributeExtractor.extractUserId(request);
        deleteUserService.deleteById(userId);
        log.info("Deleted account for user id={}", userId);
        return ResponseEntity.ok("Deleted account for user id=" + userId);
    }
}
