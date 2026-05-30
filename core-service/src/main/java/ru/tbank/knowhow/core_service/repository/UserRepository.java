package ru.tbank.knowhow.core_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tbank.knowhow.core_service.model.users.User;
import ru.tbank.knowhow.core_service.model.dto.user.response.UserProjectionForProfile;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<UserProjectionForProfile> getProjectionById(Long id);

}
