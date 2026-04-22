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
import ru.tbank.knowhow.model.dto.response.CourseDto;
import ru.tbank.knowhow.service.course.DeleteCourseService;
import ru.tbank.knowhow.service.course.GetCourseService;
import ru.tbank.knowhow.service.course.CreateCourseService;

@Slf4j
@RestController
@RequestMapping("${server.base-url.course}")
@RequiredArgsConstructor
public class CourseController {

    private final DeleteCourseService deleteCourseService;
    private final GetCourseService getCourseService;
    private final CreateCourseService createCourseService;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        deleteCourseService.deleteCourse(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseDto createCourse(
            @RequestBody @Valid CreateCourseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return createCourseService.createCourse(request, userDetails.getUsername());
    }
}