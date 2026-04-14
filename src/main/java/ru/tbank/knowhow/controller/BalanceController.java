package ru.tbank.knowhow.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.knowhow.model.dto.BalanceHistoryResponse;
import ru.tbank.knowhow.service.balance.BalanceService;

@RestController
@RequestMapping("${server.base-url.balance}")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/history")
    public ResponseEntity<BalanceHistoryResponse> getBalanceHistory(HttpServletRequest request) {
        Long id = RequestAttributeExtractor.extractUserId(request);
        return ResponseEntity.ok(balanceService.getBalanceHistory(id));
    }
}
