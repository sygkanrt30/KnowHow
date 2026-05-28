package ru.tbank.knowhow.model.dto.user.response;

import ru.tbank.knowhow.model.users.balance.Balance;
import ru.tbank.knowhow.model.ratings.Rating;
import ru.tbank.knowhow.model.users.Role;
import ru.tbank.knowhow.model.users.UserContact;

import java.util.List;

public interface UserProjectionForProfile {

    Long getId();

    String getUsername();

    UserContact getUserContact();

    Role getRole();

    Balance getBalance();

    List<Rating> getUserRatings();
}
