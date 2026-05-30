package ru.tbank.knowhow.core_service.service.users;

public interface SaveUserService {

    void save(String username, byte[] password, String email, String moderatorCode);
}
