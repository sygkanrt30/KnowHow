package ru.tbank.knowhow.service.user;

import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.model.dto.response.UsernameAndBalanceResponse;

import java.util.Optional;

public interface GetUserInfoService {

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    UsernameAndBalanceResponse getCurrentUser(Long id);
}
