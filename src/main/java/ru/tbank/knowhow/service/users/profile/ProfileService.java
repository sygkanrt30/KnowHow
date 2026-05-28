package ru.tbank.knowhow.service.users.profile;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.model.dto.course.response.CourseDto;
import ru.tbank.knowhow.model.dto.rating.response.RatingDto;
import ru.tbank.knowhow.model.dto.user.balance.request.BalanceDto;
import ru.tbank.knowhow.model.dto.user.profile.response.ProfileDto;
import ru.tbank.knowhow.model.dto.user.response.UserProjectionForProfile;
import ru.tbank.knowhow.model.ratings.Rating;
import ru.tbank.knowhow.model.users.Role;
import ru.tbank.knowhow.mappers.BalanceMapper;
import ru.tbank.knowhow.mappers.RatingMapper;
import ru.tbank.knowhow.service.purchased.PurchasedCourseService;
import ru.tbank.knowhow.service.users.GetUserService;

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
                    .email(user.getUserContact().getEmail())
                    .build();
        }

        BalanceDto balanceDto = balanceMapper.toDto(user.getBalance(), user.getId());
        List<CourseDto> purchasedCourses = purchasedCourseService.findAllPurchasedCourses(userId);
        List<RatingDto> userRatings = getRatings(user);

        return ProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getUserContact().getEmail())
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