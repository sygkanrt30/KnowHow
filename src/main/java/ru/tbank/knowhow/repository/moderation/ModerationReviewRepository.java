package ru.tbank.knowhow.repository.moderation;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tbank.knowhow.model.moderation.ModerationReview;

public interface ModerationReviewRepository extends JpaRepository<ModerationReview, Long> {

    boolean existsByCourseIdAndModeratorId(Long courseId, Long moderatorId);
}
