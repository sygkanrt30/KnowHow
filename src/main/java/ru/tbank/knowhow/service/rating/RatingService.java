package ru.tbank.knowhow.service.rating;

public interface RatingService {

    void insertRating(Long courseId, Long userId, Integer newGrade);
}
