package com.ndifreke.core_banking_api.transaction;

import com.ndifreke.core_banking_api.transaction.dto.AmountRequest;
import com.ndifreke.core_banking_api.transaction.dto.TransferRequest;
import com.ndifreke.core_banking_api.transaction.response.DepositResponse;
import com.ndifreke.core_banking_api.transaction.response.TransactionHistoryResponse;
import com.ndifreke.core_banking_api.transaction.response.TransferResponse;
import com.ndifreke.core_banking_api.transaction.response.WithdrawalResponse;
import com.ndifreke.core_banking_api.transaction.transactionType.Deposit;
import com.ndifreke.core_banking_api.transaction.transactionType.Withdrawal;
import com.ndifreke.core_banking_api.transaction.transactionType.Transfer;
import com.ndifreke.core_banking_api.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Endpoints for managing transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "Transfer funds between accounts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funds transferred successfully", content = @Content(schema = @Schema(implementation = TransferResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transferFunds(@RequestBody TransferRequest transferRequest, HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            Transfer transferFunds = transactionService.transferFunds(transferRequest.getFromAccountId(), transferRequest.getToAccountId(), transferRequest.getAmount(), transferRequest.getDescription(), authenticatedUserId);
            return ResponseEntity.ok(convertToTransactionResponse(transferFunds));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }

    @Operation(summary = "Deposit funds into an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funds deposited successfully", content = @Content(schema = @Schema(implementation = DepositResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @PostMapping("/deposit/{accountId}")
    public ResponseEntity<DepositResponse> depositFunds(@PathVariable UUID accountId, @RequestBody AmountRequest amountRequest, HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            Deposit deposit = transactionService.depositFunds(accountId, amountRequest.getAmount(), authenticatedUserId);
            return ResponseEntity.ok(convertToDepositResponse(deposit));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }

    @Operation(summary = "Withdraw funds from an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funds withdrawn successfully", content = @Content(schema = @Schema(implementation = WithdrawalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @PostMapping("/withdraw/{accountId}")
    public ResponseEntity<WithdrawalResponse> withdrawFunds(@PathVariable UUID accountId, @RequestBody AmountRequest amountRequest, HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            Withdrawal withdrawFunds = transactionService.withdrawFunds(accountId, amountRequest.getAmount(), authenticatedUserId);
            return ResponseEntity.ok(convertToWithdrawalResponse(withdrawFunds));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }

    @Operation(summary = "Get transaction history for an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction history retrieved successfully", content = @Content(schema = @Schema(implementation = TransactionHistoryResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @GetMapping("/history/{accountId}")
    public ResponseEntity<TransactionHistoryResponse> getTransactionHistory(@PathVariable UUID accountId, HttpServletRequest request) {
        UUID authenticatedUserId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            List<Transfer> transactions = transactionService.getTransactionHistory(accountId, authenticatedUserId);
            return ResponseEntity.ok(convertToTransactionHistoryResponse(transactions));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    private TransferResponse convertToTransactionResponse(Transfer transaction) {
        TransferResponse response = new TransferResponse();
        response.setTransactionId(transaction.getTransactionId());
        response.setFromAccountId(transaction.getFromAccountId());
        response.setToAccountId(transaction.getToAccountId());
        response.setAmount(transaction.getAmount());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setTransactionType(transaction.getTransactionType());
        response.setDescription(transaction.getDescription());
        return response;
    }

    private DepositResponse convertToDepositResponse(Deposit deposit) {
        DepositResponse response = new DepositResponse();
        response.setDepositId(deposit.getDepositId());
        response.setAccountId(deposit.getAccountId());
        response.setAmount(deposit.getAmount());
        response.setTransactionDate(deposit.getTransactionDate());
        response.setTransactionType(deposit.getTransactionType());
        return response;
    }

    private WithdrawalResponse convertToWithdrawalResponse(Withdrawal withdrawal) {
        WithdrawalResponse response = new WithdrawalResponse();
        response.setWithdrawalId(withdrawal.getWithdrawalId());
        response.setAccountId(withdrawal.getAccountId());
        response.setAmount(withdrawal.getAmount());
        response.setTransactionDate(withdrawal.getTransactionDate());
        response.setTransactionType(withdrawal.getTransactionType());
        return response;
    }

    private TransactionHistoryResponse convertToTransactionHistoryResponse(List<Transfer> transactions) {
        TransactionHistoryResponse response = new TransactionHistoryResponse();
        List<TransferResponse> transactionResponses = transactions.stream()
                .map(this::convertToTransactionResponse)
                .collect(Collectors.toList());
        response.setTransactions(transactionResponses);
        return response;
    }
}