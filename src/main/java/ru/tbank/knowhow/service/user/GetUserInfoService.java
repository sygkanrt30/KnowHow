package ru.tbank.knowhow.service.user;

import ru.tbank.knowhow.model.User;

import java.util.Optional;

public interface GetUserInfoService {

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    void deleteById(Long id);
}
