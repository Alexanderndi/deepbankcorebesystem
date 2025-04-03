package com.ndifreke.core_banking_api.controller;

import com.ndifreke.core_banking_api.dto.account.AccountRequest;
import com.ndifreke.core_banking_api.entity.Account;
import com.ndifreke.core_banking_api.account.AccountService;
import com.ndifreke.core_banking_api.config.CacheConfig;
import com.ndifreke.core_banking_api.exception.NotFoundException;
import com.ndifreke.core_banking_api.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * The type Account controller.
 */
@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Endpoints for managing user accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CacheConfig cacheConfig;

    /**
     * Create account response entity.
     *
     * @param accountRequest the account request
     * @param request        the request
     * @return the response entity
     */
    @Operation(summary = "Create a new account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
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

    /**
     * Gets account by id.
     *
     * @param accountId the account id
     * @param request   the request
     * @return the account by id
     */
    @Operation(summary = "Get account details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account details retrieved successfully", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccountById(@PathVariable UUID accountId, HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        Account account = accountService.getAccountById(accountId, authenticatedUserId);

        if (account == null) {
            throw new NotFoundException("Account not found with ID: " + accountId);
        }
        return ResponseEntity.ok(account);
    }

    /**
     * Gets accounts by user id.
     *
     * @param userId  the user id
     * @param request the request
     * @return the accounts by user id
     */
    @Operation(summary = "Update account details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account updated successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
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

    /**
     * Gets account balance.
     *
     * @param accountId the account id
     * @param request   the request
     * @return the account balance
     */
    @Operation(summary = "Delete an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
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

    /**
     * Update account response entity.
     *
     * @param accountId      the account id
     * @param updatedAccount the updated account
     * @param request        the request
     * @return the response entity
     */
    @PutMapping("/{accountId}")
    public ResponseEntity<Account> updateAccount(@PathVariable UUID accountId, @RequestBody Account updatedAccount, HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            accountService.validateAccountOwnership(accountId, authenticatedUserId);
            if (!accountId.equals(updatedAccount.getAccountId())) {
                return ResponseEntity.badRequest().build();
            }

            Account account = accountService.updateAccount(updatedAccount, authenticatedUserId);
            return ResponseEntity.ok(account);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }

    /**
     * Delete account response entity.
     *
     * @param accountId the account id
     * @param request   the request
     * @return the response entity
     */
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

    /**
     * Gets cache data.
     */
    @GetMapping("/cacheData")
    public void getCacheData() {
        cacheConfig.printCacheContents("accounts");
    }

}