package ru.tbank.knowhow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.knowhow.service.course.DeleteCourseService;

@RestController
@RequestMapping("${server.base-url.course}")
@RequiredArgsConstructor
class CourseController {

    private final DeleteCourseService deleteCourseService;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        deleteCourseService.deleteCourse(id);
        return ResponseEntity.ok().build();
    }
}