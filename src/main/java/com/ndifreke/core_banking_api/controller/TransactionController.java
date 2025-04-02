package com.ndifreke.core_banking_api.controller;

import com.ndifreke.core_banking_api.service.transaction.TransactionService;
import com.ndifreke.core_banking_api.dto.transaction.AmountRequest;
import com.ndifreke.core_banking_api.dto.transaction.TransferRequest;
import com.ndifreke.core_banking_api.dto.transaction.DepositResponse;
import com.ndifreke.core_banking_api.dto.transaction.TransactionHistoryResponse;
import com.ndifreke.core_banking_api.dto.transaction.TransferResponse;
import com.ndifreke.core_banking_api.dto.transaction.WithdrawalResponse;
import com.ndifreke.core_banking_api.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The type Transaction controller.
 */
@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Endpoints for managing transactions")
@Validated
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private JwtUtil jwtUtil;

    // Helper method to create error response
    private ResponseEntity<Map<String, String>> createErrorResponse(HttpStatus status, String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Transfer funds between accounts.
     */
    @Operation(summary = "Transfer funds between accounts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funds transferred successfully",
                    content = @Content(schema = @Schema(implementation = TransferResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request (e.g., invalid amount or account IDs)",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (e.g., insufficient funds or unauthorized access)",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/transfer")
    public ResponseEntity<?> transferFunds(
            @Valid @RequestBody TransferRequest transferRequest,
            HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));

        // Additional validation
        if (transferRequest.getAmount() == null || transferRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }
        if (transferRequest.getFromAccountId().equals(transferRequest.getToAccountId())) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Source and destination accounts must be different");
        }

        try {
            TransferResponse transferResponse = transactionService.transferFunds(
                    transferRequest.getFromAccountId(),
                    transferRequest.getToAccountId(),
                    transferRequest.getAmount(),
                    transferRequest.getDescription(),
                    authenticatedUserId);
            return ResponseEntity.ok(transferResponse);
        } catch (ResponseStatusException e) {
            return createErrorResponse((HttpStatus) e.getStatusCode(), e.getReason());
        }
    }

    /**
     * Deposit funds into an account.
     */
    @Operation(summary = "Deposit funds into an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funds deposited successfully",
                    content = @Content(schema = @Schema(implementation = DepositResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request (e.g., invalid amount)",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/deposit/{accountId}")
    public ResponseEntity<?> depositFunds(
            @PathVariable UUID accountId,
            @Valid @RequestBody AmountRequest amountRequest,
            HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));

        // Additional validation
        if (amountRequest.getAmount() == null || amountRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }

        try {
            DepositResponse depositResponse = transactionService.depositFunds(
                    accountId,
                    amountRequest.getAmount(),
                    authenticatedUserId);
            return ResponseEntity.ok(depositResponse);
        } catch (ResponseStatusException e) {
            return createErrorResponse((HttpStatus) e.getStatusCode(), e.getReason());
        }
    }

    /**
     * Withdraw funds from an account.
     */
    @Operation(summary = "Withdraw funds from an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funds withdrawn successfully",
                    content = @Content(schema = @Schema(implementation = WithdrawalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request (e.g., invalid amount, insufficient funds)",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (e.g., unauthorized access)",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/withdraw/{accountId}")
    public ResponseEntity<?> withdrawFunds(
            @PathVariable UUID accountId,
            @Valid @RequestBody AmountRequest amountRequest,
            HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));

        // Additional validation
        if (amountRequest.getAmount() == null || amountRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }

        try {
            WithdrawalResponse withdrawalResponse = transactionService.withdrawFunds(
                    accountId,
                    amountRequest.getAmount(),
                    authenticatedUserId);
            return ResponseEntity.ok(withdrawalResponse);
        } catch (ResponseStatusException e) {
            return createErrorResponse((HttpStatus) e.getStatusCode(), e.getReason());
        }
    }

    /**
     * Get transaction history for an account.
     */
    @Operation(summary = "Get transaction history for an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction history retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TransactionHistoryResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (e.g., unauthorized access)",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @GetMapping("/history/{accountId}")
    public ResponseEntity<?> getTransactionHistory(
            @PathVariable UUID accountId,
            HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));

        try {
            TransactionHistoryResponse transactionHistoryResponse = transactionService.getTransactionHistory(
                    accountId, authenticatedUserId);
            return ResponseEntity.ok(transactionHistoryResponse);
        } catch (ResponseStatusException e) {
            return createErrorResponse((HttpStatus) e.getStatusCode(), e.getReason());
        }
    }
}