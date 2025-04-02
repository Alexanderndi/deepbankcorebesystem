package com.ndifreke.core_banking_api.transaction;

import com.ndifreke.core_banking_api.transaction.dto.AmountRequest;
import com.ndifreke.core_banking_api.transaction.dto.TransferRequest;
import com.ndifreke.core_banking_api.transaction.response.DepositResponse;
import com.ndifreke.core_banking_api.transaction.response.TransactionHistoryResponse;
import com.ndifreke.core_banking_api.transaction.response.TransferResponse;
import com.ndifreke.core_banking_api.transaction.response.WithdrawalResponse;
import com.ndifreke.core_banking_api.transaction.transactionType.Transfer;
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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * The type Transaction controller.
 */
@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Endpoints for managing transactions")
@Validated // Required for @Valid on path variables
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Transfer funds response entity.
     *
     * @param transferRequest the transfer request
     * @param request         the request
     * @return the response entity
     */
    @Operation(summary = "Transfer funds between accounts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funds transferred successfully", content = @Content(schema = @Schema(implementation = TransferResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request (e.g., validation error)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (e.g., insufficient funds)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transferFunds(
            @Valid @RequestBody TransferRequest transferRequest, // Add @Valid
            HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            TransferResponse transferResponse = transactionService.transferFunds(
                    transferRequest.getFromAccountId(),
                    transferRequest.getToAccountId(),
                    transferRequest.getAmount(),
                    transferRequest.getDescription(),
                    authenticatedUserId);
            return ResponseEntity.ok(transferResponse);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }

    /**
     * Deposit funds response entity.
     *
     * @param accountId     the account id
     * @param amountRequest the amount request
     * @param request       the request
     * @return the response entity
     */
    @Operation(summary = "Deposit funds into an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funds deposited successfully", content = @Content(schema = @Schema(implementation = DepositResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request (e.g., validation error)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @PostMapping("/deposit/{accountId}")
    public ResponseEntity<DepositResponse> depositFunds(
            @PathVariable UUID accountId,
            @Valid @RequestBody AmountRequest amountRequest, // Add @Valid
            HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            DepositResponse depositResponse = transactionService.depositFunds(
                    accountId,
                    amountRequest.getAmount(),
                    authenticatedUserId);
            return ResponseEntity.ok(depositResponse);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }

    /**
     * Withdraw funds response entity.
     *
     * @param accountId     the account id
     * @param amountRequest the amount request
     * @param request       the request
     * @return the response entity
     */
    @Operation(summary = "Withdraw funds from an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funds withdrawn successfully", content = @Content(schema = @Schema(implementation = WithdrawalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request (e.g., validation error, insufficient funds)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @PostMapping("/withdraw/{accountId}")
    public ResponseEntity<WithdrawalResponse> withdrawFunds(
            @PathVariable UUID accountId,
            @Valid @RequestBody AmountRequest amountRequest, // Add @Valid
            HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            WithdrawalResponse withdrawalResponse = transactionService.withdrawFunds(
                    accountId,
                    amountRequest.getAmount(),
                    authenticatedUserId);
            return ResponseEntity.ok(withdrawalResponse);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }

    /**
     * Gets transaction history.
     *
     * @param accountId the account id
     * @param request   the request
     * @return the transaction history
     */
    @Operation(summary = "Get transaction history for an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction history retrieved successfully", content = @Content(schema = @Schema(implementation = TransactionHistoryResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @GetMapping("/history/{accountId}")
    public ResponseEntity<TransactionHistoryResponse> getTransactionHistory(
            @PathVariable UUID accountId,
            HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            TransactionHistoryResponse transactionHistoryResponse = transactionService.getTransactionHistory(accountId, authenticatedUserId);
            return ResponseEntity.ok(transactionHistoryResponse);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }
}