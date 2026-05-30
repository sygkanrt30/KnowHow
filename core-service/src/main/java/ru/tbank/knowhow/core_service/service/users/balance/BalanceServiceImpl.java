package ru.tbank.knowhow.core_service.service.users.balance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.core_service.model.users.balance.Balance;
import ru.tbank.knowhow.core_service.model.users.User;
import ru.tbank.knowhow.core_service.model.dto.user.balance.response.UpdateBalanceRequest;
import ru.tbank.knowhow.core_service.model.dto.user.balance.request.BalanceDto;
import ru.tbank.knowhow.core_service.model.dto.user.balance.request.BalanceHistoryResponse;
import ru.tbank.knowhow.core_service.mappers.BalanceMapper;
import ru.tbank.knowhow.core_service.service.users.GetUserService;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BalanceServiceImpl implements BalanceService {

    private final GetUserService getUserService;
    private final BalanceMapper balanceMapper;
    private final CoinsRefresher coinsRefresher;

    public BalanceServiceImpl(GetUserService getUserService, BalanceMapper balanceMapper) {
        this.getUserService = getUserService;
        this.balanceMapper = balanceMapper;
        coinsRefresher = new CoinsRefresher();
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceHistoryResponse getBalanceHistory(Long userId) {
        User user = getUser(userId);
        List<String> history = new ArrayList<>(user.getBalance().getBalanceHistories());
        return new BalanceHistoryResponse(history);
    }

    private User getUser(Long userId) {
        return getUserService.getByIdOrElseThrow(userId);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BalanceDto updateBalance(UpdateBalanceRequest request, Long userId) {
        User user = getUser(userId);
        Balance balance = user.getBalance();
        if (request.isIncreaseBalance()) {
            coinsRefresher.increase(balance, request.coins());
        } else {
            coinsRefresher.decrease(balance, request.coins());
        }
        return balanceMapper.toDto(balance, user.getId());
    }
}
