package ru.tbank.knowhow.model.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ProfileDto(
        Long id,
        String username,
        String email,
        BalanceDto balance,
        List<CourseDto> purchasedCourses,
        int countOfPurchasedCourses,
        List<RatingDto> givenGrades,
        int countOfGivenGrades
) {
}
