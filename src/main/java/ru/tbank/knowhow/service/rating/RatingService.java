package ru.tbank.knowhow.service.rating;

public interface RatingService {

    boolean addRating(Long courseId, Integer grade, String username);

    void updateRating(Long courseId, Long userId, Integer newGrade);
}
