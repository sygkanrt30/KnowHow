package ru.tbank.knowhow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tbank.knowhow.service.course.DeleteCourseService;

@RestController
@RequestMapping("${server.base-url.course}")
@RequiredArgsConstructor
@Slf4j
class CourseController {

    private final DeleteCourseService deleteCourseService;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        log.trace("DELETE /api/v1/courses/{}", id);
        deleteCourseService.deleteCourse(id);
        return ResponseEntity.ok().build();
    }
}