package ru.tbank.knowhow.core_service.service.courses.tag;


import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.knowhow.core_service.service.courses.GetCourseService;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class TagServiceImplTest {

    @Mock
    private GetCourseService getCourseService;

    @InjectMocks
    private TagServiceImpl tagService;

    @Test
    void findAllTags_ShouldReturnAllUniqueLowercaseTags() {
        String[] tags1 = {"Java", "Spring", "Reactor"};
        String[] tags2 = {"JAVA", "spring", "Kotlin"};
        String[] tags3 = {"Python", "java", "REACTOR"};
        when(getCourseService.findAllTags())
                .thenReturn(Stream.of(tags1, tags2, tags3));

        Set<String> result = tagService.findAllTags();

        assertThat(result)
                .hasSize(5)
                .containsExactlyInAnyOrder("java", "spring", "reactor", "kotlin", "python");
    }

    @Test
    void findAllTags_ShouldReturnEmptySet_WhenNoTags() {
        when(getCourseService.findAllTags()).thenReturn(Stream.empty());

        Set<String> result = tagService.findAllTags();

        assertThat(result).isEmpty();
    }

    @Test
    void findAllTags_ShouldHandleEmptyArrays() {
        String[] tags1 = {"Java", "Spring"};
        String[] tags2 = {};
        String[] tags3 = {"JAVA"};

        when(getCourseService.findAllTags())
                .thenReturn(Stream.of(tags1, tags2, tags3));

        Set<String> result = tagService.findAllTags();

        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder("java", "spring");
    }

    @Test
    void findAllTags_ShouldReturnSingleTag_WhenDuplicateAcrossArrays() {
        String[] tags1 = {"Java", "Spring"};
        String[] tags2 = {"JAVA", "spring"};
        String[] tags3 = {"java", "SPRING"};

        when(getCourseService.findAllTags())
                .thenReturn(Stream.of(tags1, tags2, tags3));

        Set<String> result = tagService.findAllTags();

        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder("java", "spring");
    }
}