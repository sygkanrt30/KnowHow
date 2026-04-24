package ru.tbank.knowhow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tbank.knowhow.model.ModerationReview;

public interface ModerationReviewRepository extends JpaRepository<ModerationReview, Long> {
}
