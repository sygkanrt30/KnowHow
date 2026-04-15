package ru.tbank.knowhow.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tbank.knowhow.model.dto.request.UpdateBalanceRequest;
import ru.tbank.knowhow.model.dto.response.BalanceHistoryResponse;
import ru.tbank.knowhow.service.balance.BalanceService;

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
