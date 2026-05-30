package ru.tbank.knowhow.core_service.controller.users.balance;

import jakarta.persistence.EntityNotFoundException;
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
import ru.tbank.knowhow.core_service.model.dto.user.balance.response.UpdateBalanceRequest;
import ru.tbank.knowhow.core_service.model.dto.user.balance.request.BalanceDto;
import ru.tbank.knowhow.core_service.model.dto.user.balance.request.BalanceHistoryResponse;
import ru.tbank.knowhow.core_service.security.AttributeName;
import ru.tbank.knowhow.core_service.service.users.balance.BalanceService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Instancio.of;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(BalanceController.class)
@Tag("integration-controller")
class BalanceControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private BalanceService balanceService;

    @Value("${server.base-url.balance}")
    private String url;

    @Test
    @WithMockUser
    @DisplayName("getBalanceHistory should return 200 with balance history")
    void shouldReturnBalanceHistory() {
        Long userId = 1L;
        List<String> history = List.of("+100 coins", "-50 coins", "+200 coins");
        BalanceHistoryResponse expectedResponse = new BalanceHistoryResponse(history);

        when(balanceService.getBalanceHistory(userId)).thenReturn(expectedResponse);

        assertThat(mockMvc.get()
                .uri(url + "/history")
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), userId))
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.history.length()").isEqualTo(3);
                    assertThat(json).extractingPath("$.history[0]").isEqualTo("+100 coins");
                    assertThat(json).extractingPath("$.history[1]").isEqualTo("-50 coins");
                    assertThat(json).extractingPath("$.history[2]").isEqualTo("+200 coins");
                });

        verify(balanceService, times(1)).getBalanceHistory(userId);
    }

    @Test
    @WithMockUser
    @DisplayName("getBalanceHistory should return empty list when user has no history")
    void shouldReturnEmptyHistory() {
        Long userId = 2L;
        BalanceHistoryResponse expectedResponse = new BalanceHistoryResponse(List.of());

        when(balanceService.getBalanceHistory(userId)).thenReturn(expectedResponse);

        assertThat(mockMvc.get()
                .uri(url + "/history")
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), userId))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .satisfies(json -> assertThat(json).extractingPath("$.history.length()").isEqualTo(0));

        verify(balanceService, times(1)).getBalanceHistory(userId);
    }

    @Test
    @DisplayName("getBalanceHistory should return 401 when user is not authenticated")
    void shouldReturn401WhenUserNotAuthenticated() {
        assertThat(mockMvc.get()
                .uri(url + "/history")
                .accept(MediaType.APPLICATION_JSON))
                .hasStatus(HttpStatus.UNAUTHORIZED);

        verify(balanceService, never()).getBalanceHistory(anyLong());
    }

    @Test
    @WithMockUser
    @DisplayName("updateBalance should return 200 with updated balance when increasing")
    void shouldUpdateBalanceWhenIncreasing() {
        Long userId = 1L;
        BalanceDto expectedBalance = of(BalanceDto.class)
                .set(field(BalanceDto::id), 1L)
                .set(field(BalanceDto::userId), userId)
                .set(field(BalanceDto::coins), 1500L)
                .create();

        when(balanceService.updateBalance(any(UpdateBalanceRequest.class), eq(userId)))
                .thenReturn(expectedBalance);

        assertThat(mockMvc.patch()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "isIncreaseBalance": true,
                            "coins": 100
                        }
                        """)
                .requestAttr(AttributeName.USER_ID.getValue(), userId)
                .with(csrf()))
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.id").isEqualTo(1);
                    assertThat(json).extractingPath("$.userId").isEqualTo(userId.intValue());
                    assertThat(json).extractingPath("$.coins").isEqualTo(1500);
                });

        verify(balanceService, times(1)).updateBalance(any(UpdateBalanceRequest.class), eq(userId));
    }

    @Test
    @WithMockUser
    @DisplayName("updateBalance should return 200 with updated balance when decreasing")
    void shouldUpdateBalanceWhenDecreasing() {
        Long userId = 2L;
        BalanceDto expectedBalance = of(BalanceDto.class)
                .set(field(BalanceDto::id), 2L)
                .set(field(BalanceDto::userId), userId)
                .set(field(BalanceDto::coins), 950L)
                .create();

        when(balanceService.updateBalance(any(UpdateBalanceRequest.class), eq(userId)))
                .thenReturn(expectedBalance);

        assertThat(mockMvc.patch()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "isIncreaseBalance": false,
                            "coins": 50
                        }
                        """)
                .requestAttr(AttributeName.USER_ID.getValue(), userId)
                .with(csrf()))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .satisfies(json -> assertThat(json).extractingPath("$.coins").isEqualTo(950));

        verify(balanceService, times(1)).updateBalance(any(UpdateBalanceRequest.class), eq(userId));
    }

    @Test
    @DisplayName("updateBalance should return 401 when user is not authenticated")
    void shouldReturn401WhenUpdatingWithoutAuth() {
        assertThat(mockMvc.patch()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "isIncreaseBalance": true,
                            "coins": 100
                        }
                        """)
                .with(csrf()))
                .hasStatus(HttpStatus.UNAUTHORIZED);

        verify(balanceService, never()).updateBalance(any(), anyLong());
    }

    @Test
    @WithMockUser
    @DisplayName("updateBalance should return 400 when coins is null")
    void shouldReturn400WhenCoinsIsNull() {
        Long userId = 1L;

        assertThat(mockMvc.patch()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "isIncreaseBalance": true,
                            "coins": null
                        }
                        """)
                .requestAttr(AttributeName.USER_ID.getValue(), userId)
                .with(csrf()))
                .hasStatus(HttpStatus.BAD_REQUEST);

        verify(balanceService, never()).updateBalance(any(), anyLong());
    }

    @Test
    @WithMockUser
    @DisplayName("updateBalance should return 400 when isIncreaseBalance is null")
    void shouldReturn400WhenIsIncreaseBalanceIsNull() {
        Long userId = 1L;

        assertThat(mockMvc.patch()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "isIncreaseBalance": null,
                            "coins": 100
                        }
                        """)
                .requestAttr(AttributeName.USER_ID.getValue(), userId)
                .with(csrf()))
                .hasStatus(HttpStatus.BAD_REQUEST);

        verify(balanceService, never()).updateBalance(any(), anyLong());
    }

    @Test
    @WithMockUser
    @DisplayName("updateBalance should return 400 when coins is zero")
    void shouldReturn400WhenCoinsIsZero() {
        Long userId = 1L;

        assertThat(mockMvc.patch()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "isIncreaseBalance": true,
                            "coins": 0
                        }
                        """)
                .requestAttr(AttributeName.USER_ID.getValue(), userId)
                .with(csrf()))
                .hasStatus(HttpStatus.BAD_REQUEST);

        verify(balanceService, never()).updateBalance(any(), anyLong());
    }

    @Test
    @WithMockUser
    @DisplayName("updateBalance should return 404 when user not found")
    void shouldReturn404WhenUserNotFound() {
        Long userId = 999L;

        when(balanceService.updateBalance(any(UpdateBalanceRequest.class), eq(userId)))
                .thenThrow(new EntityNotFoundException("User not found with id: " + userId));

        assertThat(mockMvc.patch()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "isIncreaseBalance": true,
                            "coins": 100
                        }
                        """)
                .requestAttr(AttributeName.USER_ID.getValue(), userId)
                .with(csrf()))
                .hasStatus(HttpStatus.NOT_FOUND);

        verify(balanceService, times(1)).updateBalance(any(UpdateBalanceRequest.class), eq(userId));
    }
}