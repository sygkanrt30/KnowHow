package ru.tbank.knowhow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.knowhow.model.dto.response.CourseDto;
import ru.tbank.knowhow.service.course.DeleteCourseService;
import ru.tbank.knowhow.service.course.PurchaseCourseService;

@RestController
@RequestMapping("${server.base-url.course}")
@RequiredArgsConstructor
class CourseController {

    private final DeleteCourseService deleteCourseService;
    private final PurchaseCourseService purchaseCourseService;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        deleteCourseService.deleteCourse(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/pay/{id}")
    public ResponseEntity<CourseDto> payForCourse(@PathVariable Long id, HttpServletRequest request) {
        Long userId = RequestAttributeExtractor.extractUserId(request);
        return ResponseEntity.ok(purchaseCourseService.payForCourse(id, userId));
    }
}