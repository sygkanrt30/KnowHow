package ru.tbank.knowhow.service.balance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.instancio.Instancio;
import ru.tbank.knowhow.model.Balance;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.model.dto.request.UpdateBalanceRequest;
import ru.tbank.knowhow.model.dto.response.BalanceDto;
import ru.tbank.knowhow.model.dto.response.BalanceHistoryResponse;
import ru.tbank.knowhow.model.mapper.BalanceMapper;
import ru.tbank.knowhow.service.user.GetUserService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.instancio.Select.field;

@ExtendWith(MockitoExtension.class)
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
        verify(getUserService).getByIdOrElseThrow(userId);
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
        verify(getUserService).getByIdOrElseThrow(userId);
    }

    @Test
    void updateBalance_ShouldDecreaseBalance_WhenIncreaseBalanceIsFalse() {
        Balance balance = new Balance(200L);
        long balanceId = 12L;
        balance.setId(balanceId);
        long userId = 1L;
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
        Balance balance = new Balance(100L);
        long balanceId = 1L;
        balance.setId(balanceId);
        long userId = 1L;
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
        Balance balance = new Balance(500L);
        long balanceId = 1L;
        balance.setId(balanceId);
        long userId = 1L;
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
    void updateBalance_ShouldAllowNegativeBalance() {
        Balance balance = new Balance(50L);
        long balanceId = 1L;
        balance.setId(balanceId);
        long userId = 1L;
        User user = Instancio.of(User.class)
                .set(field(User::getId), userId)
                .set(field(User::getBalance), balance)
                .create();
        UpdateBalanceRequest request = new UpdateBalanceRequest(false, 100L);
        BalanceDto expectedDto = new BalanceDto(balanceId, userId, -50L);

        when(getUserService.getByIdOrElseThrow(userId)).thenReturn(user);
        when(balanceMapper.toDto(balance, userId)).thenReturn(expectedDto);

        BalanceDto result = balanceService.updateBalance(request, userId);

        assertThat(result.coins()).isEqualTo(-50L);
        assertThat(user.getBalance().getCoins()).isEqualTo(-50L);
        verify(getUserService).getByIdOrElseThrow(userId);
        verify(balanceMapper).toDto(any(Balance.class), eq(userId));
    }
}