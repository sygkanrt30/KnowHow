package ru.tbank.knowhow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.tbank.knowhow.model.dto.request.CreateCourseRequest;
import ru.tbank.knowhow.model.dto.request.UpdateCourseRequest;
import ru.tbank.knowhow.model.dto.response.CourseDto;
import ru.tbank.knowhow.service.course.SaveCourseService;
import ru.tbank.knowhow.service.course.DeleteCourseService;
import ru.tbank.knowhow.service.course.PurchaseCourseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("${server.base-url.course}")
@RequiredArgsConstructor
public class CourseController {

    private final DeleteCourseService deleteCourseService;
    private final SaveCourseService saveCourseService;
    private final PurchaseCourseService purchaseCourseService;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        deleteCourseService.deleteCourse(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CourseDto> createCourse(
            @RequestBody @Valid CreateCourseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(saveCourseService.createCourse(request, userDetails.getUsername()));
    }

    @PostMapping("/pay/{id}")
    public ResponseEntity<CourseDto> payForCourse(@PathVariable Long id, HttpServletRequest request) {
        Long userId = RequestAttributeExtractor.extractUserId(request);
        return ResponseEntity.ok(purchaseCourseService.payForCourse(id, userId));
    }

    @PutMapping("/retry-pass-moderation/{id}")
    public ResponseEntity<CourseDto> retryPassModeration(HttpServletRequest request,
                                                         @PathVariable Long id,
                                                         @RequestBody UpdateCourseRequest updateRequest) {
        Long userId = RequestAttributeExtractor.extractUserId(request);
        return ResponseEntity.ok(saveCourseService.updateCourse(updateRequest, id, userId));
    }
}