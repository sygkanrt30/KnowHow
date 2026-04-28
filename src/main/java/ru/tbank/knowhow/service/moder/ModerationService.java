package ru.tbank.knowhow.service.moder;

import ru.tbank.knowhow.model.User;

public interface ModerationService {

    User assignModerator();

    void approveCourse(Long courseId, String moderatorUsername);

}
