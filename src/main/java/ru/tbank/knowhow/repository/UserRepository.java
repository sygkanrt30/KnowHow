package ru.tbank.knowhow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.model.dto.response.UserProjection;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<UserProjection> getProjectionById(Long id);

}
