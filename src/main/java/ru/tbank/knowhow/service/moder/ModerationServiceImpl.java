package ru.tbank.knowhow.service.moder;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tbank.knowhow.model.ModeratorLoad;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.repository.ModeratorLoadRepository;

@RequiredArgsConstructor
@Service
public class ModerationServiceImpl implements ModerationService {

    private final ModeratorLoadRepository  moderatorLoadRepository;

    @Override
    public User assignModerator() {
        ModeratorLoad moderatorLoad = moderatorLoadRepository.findModeratorWithMinLoad()
                .orElseThrow(() -> new EntityNotFoundException("No moderators available"));

        moderatorLoadRepository.incrementCoursesInModeration(moderatorLoad.getModerator().getId());

        return moderatorLoad.getModerator();
    }
}
