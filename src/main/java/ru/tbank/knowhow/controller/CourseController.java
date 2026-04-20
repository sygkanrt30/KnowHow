package ru.tbank.knowhow.controller;

import jakarta.persistence.EntityNotFoundException;
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
import ru.tbank.knowhow.service.user.UserService;
import ru.tbank.knowhow.model.User;

@Slf4j
@RestController
@RequestMapping("${server.base-url.course}")
@RequiredArgsConstructor
public class CourseController {

    private final DeleteCourseService deleteCourseService;
    private final GetCourseService getCourseService;
    private final UserService userService;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        log.debug("DELETE /api/v1/courses/{}", id);
        deleteCourseService.deleteCourse(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseDto createCourse(
            @RequestBody @Valid CreateCourseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("POST /api/v1/courses");

        String username = userDetails.getUsername();

        User author = userService.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        return getCourseService.createCourse(request, author.getId());
    }
}