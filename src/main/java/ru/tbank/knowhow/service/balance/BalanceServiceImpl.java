package ru.tbank.knowhow.service.balance;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.model.dto.response.BalanceDto;
import ru.tbank.knowhow.model.Balance;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.model.dto.request.UpdateBalanceRequest;
import ru.tbank.knowhow.model.dto.response.BalanceHistoryResponse;
import ru.tbank.knowhow.model.mapper.BalanceMapper;
import ru.tbank.knowhow.service.user.GetUserInfoService;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class BalanceServiceImpl implements BalanceService {

    private final GetUserInfoService getUserInfoService;
    private final BalanceMapper balanceMapper;

    @Override
    @Transactional(readOnly = true)
    public BalanceHistoryResponse getBalanceHistory(Long userId) {
        User user = getUser(userId);
        List<String> history = new ArrayList<>(user.getBalance().getBalanceHistories());
        return  new BalanceHistoryResponse(history);
    }

    private User getUser(Long userId) {
        return getUserInfoService.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("User not found by id: " + userId));
    }

    @Override
    @Transactional
    public BalanceDto updateBalance(UpdateBalanceRequest request, Long userId) {
        User user = getUser(userId);
        Balance balance = user.getBalance();
        long coins = balance.getCoins();
        if (request.isIncreaseBalance()) {
            coins += request.coins();
        } else  {
            coins -= request.coins();
        }
        balance.setCoins(coins);
        return balanceMapper.toDto(balance, user.getId());
    }
}
