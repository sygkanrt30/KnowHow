package ru.tbank.knowhow.core_service.service.moderation;

import jakarta.persistence.EntityNotFoundException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.knowhow.core_service.model.moderation.ModeratorLoad;
import ru.tbank.knowhow.core_service.model.users.User;
import ru.tbank.knowhow.core_service.repository.moderation.ModeratorLoadRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class ModeratorManagerImplTest {

    @Mock
    private ModeratorLoadRepository moderatorLoadRepository;

    @InjectMocks
    private ModeratorManagerImpl moderatorManager;

    @Test
    void assignModerator_ShouldReturnModeratorAndIncrementLoad_WhenModeratorExists() {
        User moderator = createModerator(1L);
        var moderatorLoad = createModeratorLoad(moderator, 0);
        when(moderatorLoadRepository.findModeratorWithMinLoad())
                .thenReturn(Optional.of(moderatorLoad));

        User result = moderatorManager.assignModerator();

        assertThat(result).isEqualTo(moderator);
        verify(moderatorLoadRepository).incrementCoursesInModeration(moderator.getId());
    }

    @Test
    void assignModerator_ShouldThrowEntityNotFoundException_WhenNoModeratorsAvailable() {
        when(moderatorLoadRepository.findModeratorWithMinLoad())
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> moderatorManager.assignModerator())
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("No moderators available");

        verify(moderatorLoadRepository, org.mockito.Mockito.never())
                .incrementCoursesInModeration(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void assignModerator_ShouldIncrementLoadForCorrectModerator_WhenMultipleModeratorsExist() {
        long moderatorId = 5L;
        User moderator = createModerator(moderatorId);
        var moderatorLoad = createModeratorLoad(moderator, 2);

        when(moderatorLoadRepository.findModeratorWithMinLoad())
                .thenReturn(Optional.of(moderatorLoad));

        moderatorManager.assignModerator();

        verify(moderatorLoadRepository).incrementCoursesInModeration(moderatorId);
    }

    private User createModerator(Long moderatorId) {
        User moderator = Instancio.create(User.class);
        moderator.setId(moderatorId);
        return moderator;
    }

    private ModeratorLoad createModeratorLoad(User moderator, int coursesInModeration) {
        var moderatorLoad = new ModeratorLoad();
        moderatorLoad.setModerator(moderator);
        moderatorLoad.setCoursesInModeration(coursesInModeration);
        return moderatorLoad;
    }
}