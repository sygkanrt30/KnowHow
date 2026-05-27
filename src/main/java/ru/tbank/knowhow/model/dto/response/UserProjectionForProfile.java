package ru.tbank.knowhow.model.dto.response;

import ru.tbank.knowhow.model.Balance;
import ru.tbank.knowhow.model.Rating;
import ru.tbank.knowhow.model.Role;
import ru.tbank.knowhow.model.UserContact;

import java.util.List;

public interface UserProjectionForProfile {

    Long getId();

    String getUsername();

    UserContact getUserContact();

    Role getRole();

    Balance getBalance();

    List<Rating> getUserRatings();
}
