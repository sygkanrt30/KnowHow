package ru.tbank.knowhow.controller.courses.tag;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import ru.tbank.knowhow.service.courses.tag.TagService;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(TagController.class)
@AutoConfigureMockMvc(addFilters = false)
@Tag("integration-controller")
class TagControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private TagService tagService;

    @Value("${server.base-url.tag}")
    private String url;

    @Test
    @DisplayName("Should return 200 if the service method is executed correctly")
    void shouldReturn200IfTheServiceMethodIsExecutedCorrectly() {
        Set<String> tags = Set.of("java", "spring", "ruby", "английский");
        when(tagService.findAllTags()).thenReturn(tags);

        assertThat(mockMvc.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
        )
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(InstanceOfAssertFactories.set(String.class))
                .hasSize(4)
                .containsExactlyInAnyOrderElementsOf(tags);

        verify(tagService).findAllTags();
    }

    @Test
    @DisplayName("Should return 500 if the service method is thrown NullPointerException")
    void shouldReturn500IfTheServiceMethodIsThrownNPE() {
        when(tagService.findAllTags()).thenThrow(NullPointerException.class);

        assertThat(mockMvc.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
        )
                .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR);

        verify(tagService).findAllTags();
    }
}