package ru.tbank.knowhow.core_service.service.purchase;

import ru.tbank.knowhow.core_service.ecxeption.AttemptPayForYourselfException;
import ru.tbank.knowhow.core_service.ecxeption.AttemptPayNotForSaleCourseException;
import ru.tbank.knowhow.core_service.ecxeption.InsufficientFundsException;
import ru.tbank.knowhow.core_service.model.courses.Course;
import ru.tbank.knowhow.core_service.model.courses.CourseStatus;
import ru.tbank.knowhow.core_service.model.users.User;

final class PurchasePreconditionValidator {

    void validate(Course course, User user, User author) {
        validateCourseIsForSale(course);
        validateCourseNotOnModeration(course);
        validateUserIsNotAuthor(user, author);
    }

    private void validateCourseIsForSale(Course course) {
        if (course.isNotForSale()) {
            throw new AttemptPayNotForSaleCourseException("Course not for sale");
        }
    }

    private void validateCourseNotOnModeration(Course course) {
        if (course.getStatus().equals(CourseStatus.ON_MODERATION)) {
            throw new IllegalStateException("Can not pay for moderated courses!");
        }
    }

    private void validateUserIsNotAuthor(User user, User author) {
        if (author.getId().equals(user.getId())) {
            throw new AttemptPayForYourselfException("You can't pay for yourself!");
        }
    }

    void validateSufficientFunds (long balance, long price) {
        if (balance <= 0L || balance < price) {
            throw new InsufficientFundsException("Insufficient funds!");
        }
    }

}
