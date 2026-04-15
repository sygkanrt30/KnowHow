package ru.tbank.knowhow.service.balance;

import ru.tbank.knowhow.controller.BalanceDto;
import ru.tbank.knowhow.model.dto.request.UpdateBalanceRequest;
import ru.tbank.knowhow.model.dto.response.BalanceHistoryResponse;

public interface BalanceService {

    BalanceHistoryResponse getBalanceHistory(Long userId);

    BalanceDto updateBalance(UpdateBalanceRequest request, Long userId);
}
