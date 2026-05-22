package ru.tbank.knowhow.service.profile;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.model.Rating;
import ru.tbank.knowhow.model.Role;
import ru.tbank.knowhow.model.dto.response.*;
import ru.tbank.knowhow.model.mapper.BalanceMapper;
import ru.tbank.knowhow.model.mapper.RatingMapper;
import ru.tbank.knowhow.service.course.purchased.PurchasedCourseService;
import ru.tbank.knowhow.service.user.GetUserService;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProfileService implements GetProfileService {

    private final GetUserService getUserService;
    private final PurchasedCourseService purchasedCourseService;
    private final BalanceMapper balanceMapper;
    private final RatingMapper ratingMapper;

    @Override
    @Transactional(readOnly = true)
    public ProfileDto getProfile(Long userId) {
        UserProjectionForProfile user = getUserService.getProjectionForProfile(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getRole().equals(Role.MODERATOR)) {
            return ProfileDto.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .build();
        }

        BalanceDto balanceDto = balanceMapper.toDto(user.getBalance(), user.getId());
        List<CourseDto> purchasedCourses = purchasedCourseService.findAllPurchasedCourses(userId);
        List<RatingDto> userRatings = getRatings(user);

        return ProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .balance(balanceDto)
                .purchasedCourses(purchasedCourses)
                .countOfPurchasedCourses(purchasedCourses.size())
                .givenGrades(userRatings)
                .countOfGivenGrades(userRatings.size())
                .build();
    }

    private List<RatingDto> getRatings(UserProjectionForProfile user) {
        return user.getUserRatings().stream()
                .sorted(Comparator.comparing(Rating::getCreatedAt, Comparator.reverseOrder()))
                .map(ratingMapper::toDto)
                .toList();
    }
}