package ru.tbank.knowhow.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.knowhow.model.dto.response.CourseDto;
import ru.tbank.knowhow.service.course.GetCourseService;
import ru.tbank.knowhow.service.user.DeleteUserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("${server.base-url.users}")
public class AccountController {

    private final GetCourseService getCourseService;
    private final DeleteUserService deleteUserService;

    @GetMapping("/purchased-courses")
    public ResponseEntity<List<CourseDto>> getPurchasedCourses(HttpServletRequest request) {
        Long userId = RequestAttributeExtractor.extractUserId(request);
        return ResponseEntity.ok(getCourseService.findAllPurchasedCourses(userId));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUserById(HttpServletRequest request) {
        Long userId = RequestAttributeExtractor.extractUserId(request);
        deleteUserService.deleteById(userId);
        return ResponseEntity.ok("Account has been deleted");
    }
}
