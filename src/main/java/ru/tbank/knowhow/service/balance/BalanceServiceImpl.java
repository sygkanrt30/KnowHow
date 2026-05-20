package ru.tbank.knowhow.service.balance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.model.Balance;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.model.dto.request.UpdateBalanceRequest;
import ru.tbank.knowhow.model.dto.response.BalanceDto;
import ru.tbank.knowhow.model.dto.response.BalanceHistoryResponse;
import ru.tbank.knowhow.model.mapper.BalanceMapper;
import ru.tbank.knowhow.service.user.GetUserService;

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
        User user = getUserService.getByIdOrElseThrow(userId);
        List<String> history = new ArrayList<>(user.getBalance().getBalanceHistories());
        return new BalanceHistoryResponse(history);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BalanceDto updateBalance(UpdateBalanceRequest request, Long userId) {
        User user = getUserService.getByIdOrElseThrow(userId);
        Balance balance = user.getBalance();
        if (request.isIncreaseBalance()) {
            coinsRefresher.increase(balance, request.coins());
        } else {
            coinsRefresher.decrease(balance, request.coins());
        }
        return balanceMapper.toDto(balance, user.getId());
    }
}
