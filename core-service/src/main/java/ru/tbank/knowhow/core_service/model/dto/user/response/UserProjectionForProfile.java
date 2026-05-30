package ru.tbank.knowhow.core_service.model.dto.user.response;

import ru.tbank.knowhow.core_service.model.users.balance.Balance;
import ru.tbank.knowhow.core_service.model.ratings.Rating;
import ru.tbank.knowhow.core_service.model.users.Role;
import ru.tbank.knowhow.core_service.model.users.UserContact;

import java.util.List;

public interface UserProjectionForProfile {

    Long getId();

    String getUsername();

    UserContact getUserContact();

    Role getRole();

    Balance getBalance();

    List<Rating> getUserRatings();
}
