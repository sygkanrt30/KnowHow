package ru.tbank.knowhow.service.balance;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.model.dto.BalanceHistoryResponse;
import ru.tbank.knowhow.service.user.GetUserInfoService;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class BalanceServiceImpl implements BalanceService {

    private final GetUserInfoService getUserInfoService;

    @Override
    @Transactional(readOnly = true)
    public BalanceHistoryResponse getBalanceHistory(Long userId) {
        User user = getUserInfoService.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("User not found by id: " + userId));
        List<String> history = new ArrayList<>(user.getBalance().getBalanceHistories());
        return  new BalanceHistoryResponse(history);
    }
}
