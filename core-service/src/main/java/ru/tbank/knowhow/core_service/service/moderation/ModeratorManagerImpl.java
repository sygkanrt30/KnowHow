package ru.tbank.knowhow.core_service.service.moderation;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.core_service.model.moderation.ModeratorLoad;
import ru.tbank.knowhow.core_service.model.users.User;
import ru.tbank.knowhow.core_service.repository.moderation.ModeratorLoadRepository;

@RequiredArgsConstructor
@Service
public class ModeratorManagerImpl implements ModeratorManager {

    private final ModeratorLoadRepository moderatorLoadRepository;

    @Override
    @Transactional
    public User assignModerator() {
        ModeratorLoad moderatorLoad = moderatorLoadRepository.findModeratorWithMinLoad()
                .orElseThrow(() -> new EntityNotFoundException("No moderators available"));

        moderatorLoadRepository.incrementCoursesInModeration(moderatorLoad.getModerator().getId());

        return moderatorLoad.getModerator();
    }
}
