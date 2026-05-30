package ru.tbank.knowhow.core_service.service.users.balance;

import org.instancio.Instancio;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.knowhow.core_service.mappers.BalanceMapper;
import ru.tbank.knowhow.core_service.model.dto.user.balance.request.BalanceDto;
import ru.tbank.knowhow.core_service.model.dto.user.balance.request.BalanceHistoryResponse;
import ru.tbank.knowhow.core_service.model.dto.user.balance.response.UpdateBalanceRequest;
import ru.tbank.knowhow.core_service.model.users.User;
import ru.tbank.knowhow.core_service.model.users.balance.Balance;
import ru.tbank.knowhow.core_service.service.users.GetUserService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class BalanceServiceImplTest {

    @Mock
    private GetUserService getUserService;

    @Mock
    private BalanceMapper balanceMapper;

    @InjectMocks
    private BalanceServiceImpl balanceService;

    @Test
    void getBalanceHistory_ShouldReturnHistoryResponse() {
        long userId = 1L;
        User user = Instancio.of(User.class)
                .set(field(User::getId), userId)
                .set(field(User::getBalance), Instancio.create(Balance.class))
                .create();
        List<String> history = List.of("+100", "-50", "+200");
        user.getBalance().setBalanceHistories(new ArrayList<>(history));
        when(getUserService.getByIdOrElseThrow(userId)).thenReturn(user);

        BalanceHistoryResponse response = balanceService.getBalanceHistory(userId);

        assertThat(response.history()).containsExactly("+100", "-50", "+200");
    }

    @Test
    void getBalanceHistory_ShouldReturnEmptyHistory_WhenNoHistoryExists() {
        long userId = 1L;
        User user = Instancio.of(User.class)
                .set(field(User::getId), userId)
                .set(field(User::getBalance), Instancio.create(Balance.class))
                .create();
        user.getBalance().setBalanceHistories(new ArrayList<>());
        when(getUserService.getByIdOrElseThrow(userId)).thenReturn(user);

        BalanceHistoryResponse response = balanceService.getBalanceHistory(userId);

        assertThat(response.history()).isEmpty();
    }

    @Test
    void updateBalance_ShouldDecreaseBalance_WhenIncreaseBalanceIsFalse() {
        long balanceId = 12L;
        long userId = 1L;
        Balance balance = new Balance(200L);
        balance.setId(balanceId);
        User user = Instancio.of(User.class)
                .set(field(User::getId), userId)
                .set(field(User::getBalance), balance)
                .create();
        UpdateBalanceRequest request = new UpdateBalanceRequest(false, 75L);
        BalanceDto expectedDto = new BalanceDto(balanceId, userId, 125L);

        when(getUserService.getByIdOrElseThrow(userId)).thenReturn(user);
        when(balanceMapper.toDto(balance, userId)).thenReturn(expectedDto);

        BalanceDto result = balanceService.updateBalance(request, userId);

        assertThat(result.coins()).isEqualTo(125L);
        assertThat(user.getBalance().getCoins()).isEqualTo(125L);
        verify(getUserService).getByIdOrElseThrow(userId);
        verify(balanceMapper).toDto(any(Balance.class), eq(userId));
    }


    @Test
    void updateBalance_ShouldIncreaseBalance_WhenIncreaseBalanceIsTrue() {
        long userId = 1L;
        long balanceId = 1L;
        Balance balance = new Balance(100L);
        balance.setId(balanceId);
        User user = Instancio.of(User.class)
                .set(field(User::getId), userId)
                .set(field(User::getBalance), balance)
                .create();
        UpdateBalanceRequest request = new UpdateBalanceRequest(true, 50L);
        BalanceDto expectedDto = new BalanceDto(balanceId, userId, 150L);

        when(getUserService.getByIdOrElseThrow(userId)).thenReturn(user);
        when(balanceMapper.toDto(balance, userId)).thenReturn(expectedDto);

        BalanceDto result = balanceService.updateBalance(request, userId);

        assertThat(result.coins()).isEqualTo(150L);
        assertThat(user.getBalance().getCoins()).isEqualTo(150L);
        verify(getUserService).getByIdOrElseThrow(userId);
        verify(balanceMapper).toDto(any(Balance.class), eq(userId));
    }

    @Test
    void updateBalance_ShouldHandleMinimalPositiveAmount() {
        long userId = 1L;
        long balanceId = 1L;
        Balance balance = new Balance(500L);
        balance.setId(balanceId);
        User user = Instancio.of(User.class)
                .set(field(User::getId), userId)
                .set(field(User::getBalance), balance)
                .create();
        UpdateBalanceRequest request = new UpdateBalanceRequest(true, 1L);
        BalanceDto expectedDto = new BalanceDto(balanceId, userId, 501L);

        when(getUserService.getByIdOrElseThrow(userId)).thenReturn(user);
        when(balanceMapper.toDto(balance, userId)).thenReturn(expectedDto);

        BalanceDto result = balanceService.updateBalance(request, userId);

        assertThat(result.coins()).isEqualTo(501L);
        assertThat(user.getBalance().getCoins()).isEqualTo(501L);
        verify(getUserService).getByIdOrElseThrow(userId);
        verify(balanceMapper).toDto(any(Balance.class), eq(userId));
    }

    @Test
    void updateBalance_ShouldThrowException_WhenBalanceMightBeNegativeAfterUpdate() {
        long userId = 1L;
        long balanceId = 1L;
        Balance balance = new Balance(50L);
        balance.setId(balanceId);
        User user = Instancio.of(User.class)
                .set(field(User::getId), userId)
                .set(field(User::getBalance), balance)
                .create();
        UpdateBalanceRequest request = new UpdateBalanceRequest(false, 100L);
        when(getUserService.getByIdOrElseThrow(userId)).thenReturn(user);

        assertThrows(IllegalArgumentException.class, () -> balanceService.updateBalance(request, userId));
    }
}