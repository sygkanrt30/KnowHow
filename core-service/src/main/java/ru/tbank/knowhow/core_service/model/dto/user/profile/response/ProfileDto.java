package ru.tbank.knowhow.core_service.model.dto.user.profile.response;

import lombok.Builder;
import ru.tbank.knowhow.core_service.model.dto.course.response.CourseDto;
import ru.tbank.knowhow.core_service.model.dto.user.balance.request.BalanceDto;
import ru.tbank.knowhow.core_service.model.dto.rating.response.RatingDto;

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
