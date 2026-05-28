package ru.tbank.knowhow.service.users;

public interface SaveUserService {

    void save(String username, byte[] password, String email, String moderatorCode);
}
