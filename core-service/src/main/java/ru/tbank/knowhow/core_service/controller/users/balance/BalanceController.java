package ru.tbank.knowhow.core_service.controller.users.balance;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tbank.knowhow.core_service.model.dto.user.balance.response.UpdateBalanceRequest;
import ru.tbank.knowhow.core_service.model.dto.user.balance.request.BalanceDto;
import ru.tbank.knowhow.core_service.model.dto.user.balance.request.BalanceHistoryResponse;
import ru.tbank.knowhow.core_service.service.users.balance.BalanceService;
import ru.tbank.knowhow.core_service.util.RequestAttributeExtractor;

@RestController
@RequestMapping("${server.base-url.balance}")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/history")
    public ResponseEntity<BalanceHistoryResponse> getBalanceHistory(HttpServletRequest request) {
        Long userId = RequestAttributeExtractor.extractUserId(request);
        return ResponseEntity.ok(balanceService.getBalanceHistory(userId));
    }

    @PatchMapping
    public ResponseEntity<BalanceDto> updateBalance(HttpServletRequest request,
                                                    @RequestBody UpdateBalanceRequest updateBalanceRequest) {
        Long userId = RequestAttributeExtractor.extractUserId(request);
        return ResponseEntity.ok(balanceService.updateBalance(updateBalanceRequest, userId));
    }
}
