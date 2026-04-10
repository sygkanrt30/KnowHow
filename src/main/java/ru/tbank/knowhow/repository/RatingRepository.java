package ru.tbank.knowhow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tbank.knowhow.model.Rating;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
}
