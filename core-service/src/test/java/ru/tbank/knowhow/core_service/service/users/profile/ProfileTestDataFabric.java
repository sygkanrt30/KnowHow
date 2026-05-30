package ru.tbank.knowhow.core_service.service.users.profile;

import org.instancio.Instancio;
import ru.tbank.knowhow.core_service.model.ratings.Rating;
import ru.tbank.knowhow.core_service.model.dto.course.response.CourseDto;
import ru.tbank.knowhow.core_service.model.dto.rating.response.RatingDto;

import java.time.Instant;
import java.util.List;

import static org.instancio.Select.field;

final class ProfileTestDataFabric {

    static List<Rating> createRatings() {
        Rating ratingOld = Instancio.of(Rating.class)
                .set(field(Rating::getId), 1L)
                .set(field(Rating::getGrade), 3)
                .set(field(Rating::getCreatedAt), Instant.parse("2024-01-01T10:00:00Z"))
                .create();

        Rating ratingMiddle = Instancio.of(Rating.class)
                .set(field(Rating::getId), 2L)
                .set(field(Rating::getGrade), 4)
                .set(field(Rating::getCreatedAt), Instant.parse("2024-01-10T10:00:00Z"))
                .create();

        Rating ratingNew = Instancio.of(Rating.class)
                .set(field(Rating::getId), 3L)
                .set(field(Rating::getGrade),  5)
                .set(field(Rating::getCreatedAt), Instant.parse("2024-01-20T10:00:00Z"))
                .create();
        return List.of(ratingOld, ratingMiddle, ratingNew);
    }

    static List<RatingDto> createRatingDtos() {
        RatingDto ratingDtoOld = Instancio.of(RatingDto.class)
                .set(field(RatingDto::id), 1L)
                .set(field(RatingDto::grade), 3)
                .create();

        RatingDto ratingDtoMiddle = Instancio.of(RatingDto.class)
                .set(field(RatingDto::id), 2L)
                .set(field(RatingDto::grade), 4)
                .create();

        RatingDto ratingDtoNew = Instancio.of(RatingDto.class)
                .set(field(RatingDto::id), 3L)
                .set(field(RatingDto::grade),  5)
                .create();

        return List.of(ratingDtoOld, ratingDtoMiddle, ratingDtoNew);
    }

    static List<CourseDto> createCourseDtos() {
        CourseDto course1 = Instancio.of(CourseDto.class)
                .set(field(CourseDto::id), 1L)
                .set(field(CourseDto::title), "Course 1")
                .set(field(CourseDto::price), 100L)
                .create();
        CourseDto course2 = Instancio.of(CourseDto.class)
                .set(field(CourseDto::id), 2L)
                .set(field(CourseDto::title), "Course 2")
                .set(field(CourseDto::price), 200L)
                .create();
        return List.of(course1, course2);
    }
}
