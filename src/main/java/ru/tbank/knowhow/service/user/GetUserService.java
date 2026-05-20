package ru.tbank.knowhow.service.user;

import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.model.dto.response.UserProjectionForProfile;
import ru.tbank.knowhow.model.dto.response.UsernameAndBalanceResponse;

import java.util.Optional;

public interface GetUserService {

    Optional<User> findByUsername(String username);

    User getByUsernameOrElseThrow(String username);

    Optional<User> findById(Long id);

    User getByIdOrElseThrow(Long id);

    Optional<UserProjectionForProfile> getProjectionForProfile(Long id);
  
    UsernameAndBalanceResponse getCurrentUser(Long id);
}
