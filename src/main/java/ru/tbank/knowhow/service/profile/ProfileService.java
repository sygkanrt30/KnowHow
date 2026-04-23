package ru.tbank.knowhow.service.profile;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.model.Rating;
import ru.tbank.knowhow.model.dto.response.*;
import ru.tbank.knowhow.model.mapper.BalanceMapper;
import ru.tbank.knowhow.model.mapper.RatingMapper;
import ru.tbank.knowhow.repository.UserRepository;
import ru.tbank.knowhow.service.course.GetCourseService;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProfileService implements GetProfileService {

    private final UserRepository userRepository;
    private final GetCourseService getCourseService;
    private final BalanceMapper balanceMapper;
    private final RatingMapper ratingMapper;

    @Override
    @Transactional(readOnly = true)
    public ProfileDto getProfile(Long userId) {
        UserProjection user = userRepository.getProjectionById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        BalanceDto balanceDto = balanceMapper.toDto(user.getBalance(), user.getId());
        List<CourseDto> purchasedCourses = getCourseService.findAllPurchasedCourses(userId);
        List<RatingDto> userRatings = user.getUserRatings().stream()
                .sorted(Comparator.comparing(Rating::getCreatedAt, Comparator.reverseOrder()))
                .map(ratingMapper::toDto)
                .toList();

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
}