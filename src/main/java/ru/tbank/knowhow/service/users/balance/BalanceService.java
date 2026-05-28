package ru.tbank.knowhow.service.users.balance;

import ru.tbank.knowhow.model.dto.user.balance.request.BalanceDto;
import ru.tbank.knowhow.model.dto.user.balance.response.UpdateBalanceRequest;
import ru.tbank.knowhow.model.dto.user.balance.request.BalanceHistoryResponse;

public interface BalanceService {

    BalanceHistoryResponse getBalanceHistory(Long userId);

    BalanceDto updateBalance(UpdateBalanceRequest request, Long userId);
}
