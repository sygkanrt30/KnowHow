package ru.tbank.knowhow.core_service.service.users.profile;

import ru.tbank.knowhow.core_service.model.dto.user.profile.response.ProfileDto;

public interface GetProfileService {

    ProfileDto getProfile(Long userId);
}
