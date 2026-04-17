package ru.tbank.knowhow.service.user;

public interface SaveUserService {

    void save(String username, byte[] password, String email, String moderatorCode);
}
