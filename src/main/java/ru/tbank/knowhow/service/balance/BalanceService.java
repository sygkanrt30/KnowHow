package ru.tbank.knowhow.service.balance;

import ru.tbank.knowhow.model.dto.BalanceHistoryResponse;

public interface BalanceService {

    BalanceHistoryResponse getBalanceHistory(Long userId);
}
