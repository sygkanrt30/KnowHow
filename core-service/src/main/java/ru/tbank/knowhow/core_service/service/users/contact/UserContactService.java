package ru.tbank.knowhow.core_service.service.users.contact;

public interface UserContactService {

    void updateEmail(String email, Long userId);

    void verifyEmail(Long userId);
}
