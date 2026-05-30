package ru.tbank.knowhow.core_service.controller.users.account;

import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import ru.tbank.knowhow.core_service.model.dto.course.response.CourseDto;
import ru.tbank.knowhow.core_service.security.AttributeName;
import ru.tbank.knowhow.core_service.service.purchased.PurchasedCourseService;
import ru.tbank.knowhow.core_service.service.users.DeleteUserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(AccountController.class)
@Tag("integration-controller")
class AccountControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private PurchasedCourseService purchasedCourseService;

    @MockitoBean
    private DeleteUserService deleteUserService;

    @Value("${server.base-url.users}")
    private String url;

    @Test
    @WithMockUser
    @DisplayName("getPurchasedCourses should return 200 if service method is executed correctly")
    void shouldReturn200IfTheServiceMethodIsExecutedCorrectly() {
        Long testUserId = 777L;
        List<CourseDto> purchasedCourses = Instancio.ofList(CourseDto.class)
                .size(15)
                .create();
        when(purchasedCourseService.findAllPurchasedCourses(testUserId)).thenReturn(purchasedCourses);

        assertThat(mockMvc.get()
                .uri(url + "/purchased-courses")
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), testUserId)
        )
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(InstanceOfAssertFactories.list(CourseDto.class))
                .hasSize(15);

        verify(purchasedCourseService).findAllPurchasedCourses(testUserId);
    }

    @Test
    @WithMockUser
    @DisplayName("getPurchasedCourses should return 500 if service method is thrown NPE")
    void shouldReturn500IfTheServiceMethodIsThrownNPE() {
        Long testUserId = 777L;
        when(purchasedCourseService.findAllPurchasedCourses(testUserId)).thenThrow(NullPointerException.class);

        assertThat(mockMvc.get()
                .uri(url + "/purchased-courses")
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), testUserId)
        )
                .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR);

        verify(purchasedCourseService).findAllPurchasedCourses(testUserId);
    }

    @Test
    @WithMockUser
    @DisplayName("deleteUserById should return 200 if service method is executed correctly")
    void deleteUserByIdShouldReturn200IfTheServiceMethodIsExecutedCorrectly() {
        Long testUserId = 777L;

        assertThat(mockMvc.delete()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), testUserId)
                .with(csrf())
        )
                .hasStatusOk();

        verify(deleteUserService).deleteById(testUserId);
    }

    @Test
    @WithMockUser
    @DisplayName("deleteUserById should return 404 if service method is thrown EntityNotFoundException")
    void deleteUserByIdShouldReturn404IfTheServiceMethodIsThrownEntityNotFoundException() {
        Long testUserId = 777L;
        doThrow(new EntityNotFoundException("User not found with id: " + testUserId))
                .when(deleteUserService).deleteById(testUserId);

        assertThat(mockMvc.delete()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), testUserId)
                .with(csrf())
        )
                .hasStatus(HttpStatus.NOT_FOUND);

        verify(deleteUserService).deleteById(testUserId);
    }

}