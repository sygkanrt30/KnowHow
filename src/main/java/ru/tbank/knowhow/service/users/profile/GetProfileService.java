package ru.tbank.knowhow.service.users.profile;

import ru.tbank.knowhow.model.dto.user.profile.response.ProfileDto;

public interface GetProfileService {

    ProfileDto getProfile(Long userId);
}
