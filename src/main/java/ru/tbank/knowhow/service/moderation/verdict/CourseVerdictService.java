package ru.tbank.knowhow.service.moderation.verdict;

public interface CourseVerdictService {

    void approveCourse(Long courseId, String moderatorUsername);

    void rejectCourse(Long courseId, String moderatorUsername, String rejectionReason);
}
