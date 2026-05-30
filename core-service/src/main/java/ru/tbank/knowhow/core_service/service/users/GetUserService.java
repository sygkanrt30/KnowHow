package ru.tbank.knowhow.core_service.service.users;

import ru.tbank.knowhow.core_service.model.users.User;
import ru.tbank.knowhow.core_service.model.dto.user.response.UserProjectionForProfile;
import ru.tbank.knowhow.core_service.model.dto.user.response.UsernameAndBalanceResponse;

import java.util.Optional;

public interface GetUserService {

    Optional<User> findByUsername(String username);

    User getByUsernameOrElseThrow(String username);

    Optional<User> findById(Long id);

    User getByIdOrElseThrow(Long id);

    Optional<UserProjectionForProfile> getProjectionForProfile(Long id);
  
    UsernameAndBalanceResponse getCurrentUser(Long id);
}
