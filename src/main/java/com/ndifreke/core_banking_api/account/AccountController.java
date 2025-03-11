package com.ndifreke.core_banking_api.account;

import com.ndifreke.core_banking_api.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody AccountRequest accountRequest, HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        Account account = new Account();
        account.setUserId(accountRequest.getUserId());
        account.setAccountType(accountRequest.getAccountType());

        try {
            Account createdAccount = accountService.createAccount(account, accountRequest.getInitialBalance(), authenticatedUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccountById(@PathVariable UUID accountId, HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            Account account = accountService.getAccountById(accountId, authenticatedUserId);
            return ResponseEntity.ok(account);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Account>> getAccountsByUserId(@PathVariable UUID userId, HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            List<Account> accounts = accountService.getAccountsByUserId(userId, authenticatedUserId);
            return ResponseEntity.ok(accounts);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable UUID accountId, HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            BigDecimal balance = accountService.getAccountBalance(accountId, authenticatedUserId);
            return ResponseEntity.ok(balance);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<Account> updateAccount(@PathVariable UUID accountId, @RequestBody Account updatedAccount, HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            accountService.validateAccountOwnership(accountId, authenticatedUserId);
            if (!accountId.equals(updatedAccount.getAccountId())) {
                return ResponseEntity.badRequest().build();
            }

            Account account = accountService.updateAccount(updatedAccount);
            return ResponseEntity.ok(account);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID accountId, HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            accountService.deleteAccount(accountId, authenticatedUserId);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    static class AccountRequest {
        private UUID userId;
        private String accountType;
        private BigDecimal initialBalance;

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public String getAccountType() {
            return accountType;
        }

        public void setAccountType(String accountType) {
            this.accountType = accountType;
        }

        public BigDecimal getInitialBalance() {
            return initialBalance;
        }

        public void setInitialBalance(BigDecimal initialBalance) {
            this.initialBalance = initialBalance;
        }
    }
}