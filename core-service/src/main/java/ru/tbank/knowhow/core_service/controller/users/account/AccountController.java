package ru.tbank.knowhow.core_service.controller.users.account;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.knowhow.core_service.model.dto.course.response.CourseDto;
import ru.tbank.knowhow.core_service.service.purchased.PurchasedCourseService;
import ru.tbank.knowhow.core_service.service.users.DeleteUserService;
import ru.tbank.knowhow.core_service.util.RequestAttributeExtractor;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${server.base-url.users}")
public class AccountController {

    private final PurchasedCourseService purchasedCourseService;
    private final DeleteUserService deleteUserService;

    @GetMapping("/purchased-courses")
    public ResponseEntity<List<CourseDto>> getPurchasedCourses(HttpServletRequest request) {
        Long userId = RequestAttributeExtractor.extractUserId(request);
        return ResponseEntity.ok(purchasedCourseService.findAllPurchasedCourses(userId));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUserById(HttpServletRequest request) {
        Long userId = RequestAttributeExtractor.extractUserId(request);
        deleteUserService.deleteById(userId);
        return ResponseEntity.ok("Account has been deleted");
    }
}
