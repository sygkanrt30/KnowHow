package ru.tbank.knowhow.service.profile;

import ru.tbank.knowhow.model.dto.response.ProfileDto;

public interface GetProfileService {

    ProfileDto getProfile(Long userId);
}
