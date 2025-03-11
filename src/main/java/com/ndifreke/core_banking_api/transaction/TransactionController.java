package com.ndifreke.core_banking_api.transaction;

import com.ndifreke.core_banking_api.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/transfer")
    public ResponseEntity<String> transferFunds(@RequestBody TransferRequest transferRequest, HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            transactionService.transferFunds(transferRequest.getFromAccountId(), transferRequest.getToAccountId(), transferRequest.getAmount(), authenticatedUserId);
            return ResponseEntity.ok("Funds transferred successfully");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @PostMapping("/deposit/{accountId}")
    public ResponseEntity<String> depositFunds(@PathVariable UUID accountId, @RequestBody AmountRequest amountRequest, HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            transactionService.depositFunds(accountId, amountRequest.getAmount(), authenticatedUserId);
            return ResponseEntity.ok("Funds deposited successfully");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @PostMapping("/withdraw/{accountId}")
    public ResponseEntity<String> withdrawFunds(@PathVariable UUID accountId, @RequestBody AmountRequest amountRequest, HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            transactionService.withdrawFunds(accountId, amountRequest.getAmount(), authenticatedUserId);
            return ResponseEntity.ok("Funds withdrawn successfully");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @GetMapping("/history/{accountId}")
    public ResponseEntity<List<Object>> getTransactionHistory(@PathVariable UUID accountId, HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            List<Object> transactions = transactionService.getTransactionHistory(accountId, authenticatedUserId);
            return ResponseEntity.ok(transactions);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }
}