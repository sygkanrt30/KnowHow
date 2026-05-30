package ru.tbank.knowhow.core_service.service.rating;

public interface RatingService {

    void insertRating(Long courseId, Long userId, Integer newGrade);
}
