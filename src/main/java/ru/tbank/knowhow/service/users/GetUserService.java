package ru.tbank.knowhow.service.users;

import ru.tbank.knowhow.model.users.User;
import ru.tbank.knowhow.model.dto.user.response.UserProjectionForProfile;
import ru.tbank.knowhow.model.dto.user.response.UsernameAndBalanceResponse;

import java.util.Optional;

public interface GetUserService {

    Optional<User> findByUsername(String username);

    User getByUsernameOrElseThrow(String username);

    Optional<User> findById(Long id);

    User getByIdOrElseThrow(Long id);

    Optional<UserProjectionForProfile> getProjectionForProfile(Long id);
  
    UsernameAndBalanceResponse getCurrentUser(Long id);
}
