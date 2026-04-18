package ru.tbank.knowhow.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.knowhow.model.dto.request.SortRequest;
import ru.tbank.knowhow.model.dto.response.CourseDto;
import ru.tbank.knowhow.service.course.GetCourseService;

@RestController
@RequiredArgsConstructor
@Validated
public class AccountController {

    private final GetCourseService getCourseService;

    @GetMapping("${server.base-url.users}/purchased-course")
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
}
