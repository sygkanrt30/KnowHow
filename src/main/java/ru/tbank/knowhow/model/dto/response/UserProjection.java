package ru.tbank.knowhow.model.dto.response;

import ru.tbank.knowhow.model.Balance;
import ru.tbank.knowhow.model.Rating;
import ru.tbank.knowhow.model.Role;

import java.util.List;

public interface UserProjection {

    Long getId();

    String getUsername();

    String getEmail();

    Role getRole();

    Balance getBalance();

    List<Rating> getUserRatings();
}
