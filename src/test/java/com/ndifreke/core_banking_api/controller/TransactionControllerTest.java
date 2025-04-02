package com.ndifreke.core_banking_api.controller;

import com.ndifreke.core_banking_api.dto.transaction.*;
import com.ndifreke.core_banking_api.service.transaction.TransactionService;
import com.ndifreke.core_banking_api.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @InjectMocks
    private TransactionController transactionController;

    @Mock
    private TransactionService transactionService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    private UUID userId;
    private UUID fromAccountId;
    private UUID toAccountId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        fromAccountId = UUID.randomUUID();
        toAccountId = UUID.randomUUID();

        // Mock JWT behavior
        when(jwtUtil.getTokenFromRequest(request)).thenReturn("mocked-token");
        when(jwtUtil.extractUserId("mocked-token")).thenReturn(userId);
    }

    // --- Transfer Funds Tests ---

    @Test
    void transferFunds_Success() {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(fromAccountId);
        transferRequest.setToAccountId(toAccountId);
        transferRequest.setAmount(BigDecimal.valueOf(100.00));
        transferRequest.setDescription("Test transfer");

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setTransactionId(UUID.randomUUID());
        transferResponse.setFromAccountId(fromAccountId);
        transferResponse.setToAccountId(toAccountId);
        transferResponse.setAmount(BigDecimal.valueOf(100.00));

        when(transactionService.transferFunds(fromAccountId, toAccountId, BigDecimal.valueOf(100.00), "Test transfer", userId))
                .thenReturn(transferResponse);

        ResponseEntity<?> response = transactionController.transferFunds(transferRequest, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(transferResponse, response.getBody());
        verify(transactionService, times(1)).transferFunds(fromAccountId, toAccountId, BigDecimal.valueOf(100.00), "Test transfer", userId);
    }

    @Test
    void transferFunds_InvalidAmount() {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(fromAccountId);
        transferRequest.setToAccountId(toAccountId);
        transferRequest.setAmount(BigDecimal.ZERO);
        transferRequest.setDescription("Test transfer");

        ResponseEntity<?> response = transactionController.transferFunds(transferRequest, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Amount must be positive", body.get("message"));
        verify(transactionService, never()).transferFunds(any(), any(), any(), any(), any());
    }

    @Test
    void transferFunds_SameAccount() {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(fromAccountId);
        transferRequest.setToAccountId(fromAccountId);
        transferRequest.setAmount(BigDecimal.valueOf(100.00));
        transferRequest.setDescription("Test transfer");

        ResponseEntity<?> response = transactionController.transferFunds(transferRequest, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Source and destination accounts must be different", body.get("message"));
        verify(transactionService, never()).transferFunds(any(), any(), any(), any(), any());
    }

    @Test
    void transferFunds_Forbidden() {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(fromAccountId);
        transferRequest.setToAccountId(toAccountId);
        transferRequest.setAmount(BigDecimal.valueOf(100.00));
        transferRequest.setDescription("Test transfer");

        when(transactionService.transferFunds(fromAccountId, toAccountId, BigDecimal.valueOf(100.00), "Test transfer", userId))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));

        ResponseEntity<?> response = transactionController.transferFunds(transferRequest, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Forbidden", body.get("error"));
        assertEquals("Access denied", body.get("message"));
        verify(transactionService, times(1)).transferFunds(fromAccountId, toAccountId, BigDecimal.valueOf(100.00), "Test transfer", userId);
    }

    @Test
    void transferFunds_NotFound() {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(fromAccountId);
        transferRequest.setToAccountId(toAccountId);
        transferRequest.setAmount(BigDecimal.valueOf(100.00));
        transferRequest.setDescription("Test transfer");

        when(transactionService.transferFunds(fromAccountId, toAccountId, BigDecimal.valueOf(100.00), "Test transfer", userId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination account not found"));

        ResponseEntity<?> response = transactionController.transferFunds(transferRequest, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Not Found", body.get("error"));
        assertEquals("Destination account not found", body.get("message"));
        verify(transactionService, times(1)).transferFunds(fromAccountId, toAccountId, BigDecimal.valueOf(100.00), "Test transfer", userId);
    }

    // --- Deposit Funds Tests ---

    @Test
    void depositFunds_Success() {
        UUID accountId = UUID.randomUUID();
        AmountRequest amountRequest = new AmountRequest();
        amountRequest.setAmount(BigDecimal.valueOf(200.00));
        amountRequest.setDescription("Test deposit");

        DepositResponse depositResponse = new DepositResponse();
        depositResponse.setDepositId(UUID.randomUUID());
        depositResponse.setAccountId(accountId);
        depositResponse.setAmount(BigDecimal.valueOf(200.00));

        when(transactionService.depositFunds(accountId, BigDecimal.valueOf(200.00), userId))
                .thenReturn(depositResponse);

        ResponseEntity<?> response = transactionController.depositFunds(accountId, amountRequest, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(depositResponse, response.getBody());
        verify(transactionService, times(1)).depositFunds(accountId, BigDecimal.valueOf(200.00), userId);
    }

    @Test
    void depositFunds_InvalidAmount() {
        UUID accountId = UUID.randomUUID();
        AmountRequest amountRequest = new AmountRequest();
        amountRequest.setAmount(BigDecimal.ZERO);
        amountRequest.setDescription("Test deposit");

        ResponseEntity<?> response = transactionController.depositFunds(accountId, amountRequest, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Amount must be positive", body.get("message"));
        verify(transactionService, never()).depositFunds(any(), any(), any());
    }

    @Test
    void depositFunds_NotFound() {
        UUID accountId = UUID.randomUUID();
        AmountRequest amountRequest = new AmountRequest();
        amountRequest.setAmount(BigDecimal.valueOf(200.00));
        amountRequest.setDescription("Test deposit");

        when(transactionService.depositFunds(accountId, BigDecimal.valueOf(200.00), userId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        ResponseEntity<?> response = transactionController.depositFunds(accountId, amountRequest, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Not Found", body.get("error"));
        assertEquals("Account not found", body.get("message"));
        verify(transactionService, times(1)).depositFunds(accountId, BigDecimal.valueOf(200.00), userId);
    }

    // --- Withdraw Funds Tests ---

    @Test
    void withdrawFunds_Success() {
        UUID accountId = UUID.randomUUID();
        AmountRequest amountRequest = new AmountRequest();
        amountRequest.setAmount(BigDecimal.valueOf(50.00));
        amountRequest.setDescription("Test withdrawal");

        WithdrawalResponse withdrawalResponse = new WithdrawalResponse();
        withdrawalResponse.setWithdrawalId(UUID.randomUUID());
        withdrawalResponse.setAccountId(accountId);
        withdrawalResponse.setAmount(BigDecimal.valueOf(50.00));

        when(transactionService.withdrawFunds(accountId, BigDecimal.valueOf(50.00), userId))
                .thenReturn(withdrawalResponse);

        ResponseEntity<?> response = transactionController.withdrawFunds(accountId, amountRequest, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(withdrawalResponse, response.getBody());
        verify(transactionService, times(1)).withdrawFunds(accountId, BigDecimal.valueOf(50.00), userId);
    }

    @Test
    void withdrawFunds_InvalidAmount() {
        UUID accountId = UUID.randomUUID();
        AmountRequest amountRequest = new AmountRequest();
        amountRequest.setAmount(BigDecimal.ZERO);
        amountRequest.setDescription("Test withdrawal");

        ResponseEntity<?> response = transactionController.withdrawFunds(accountId, amountRequest, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Amount must be positive", body.get("message"));
        verify(transactionService, never()).withdrawFunds(any(), any(), any());
    }

    @Test
    void withdrawFunds_InsufficientFunds() {
        UUID accountId = UUID.randomUUID();
        AmountRequest amountRequest = new AmountRequest();
        amountRequest.setAmount(BigDecimal.valueOf(50.00));
        amountRequest.setDescription("Test withdrawal");

        when(transactionService.withdrawFunds(accountId, BigDecimal.valueOf(50.00), userId))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds"));

        ResponseEntity<?> response = transactionController.withdrawFunds(accountId, amountRequest, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Insufficient funds", body.get("message"));
        verify(transactionService, times(1)).withdrawFunds(accountId, BigDecimal.valueOf(50.00), userId);
    }

    @Test
    void withdrawFunds_Forbidden() {
        UUID accountId = UUID.randomUUID();
        AmountRequest amountRequest = new AmountRequest();
        amountRequest.setAmount(BigDecimal.valueOf(50.00));
        amountRequest.setDescription("Test withdrawal");

        when(transactionService.withdrawFunds(accountId, BigDecimal.valueOf(50.00), userId))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));

        ResponseEntity<?> response = transactionController.withdrawFunds(accountId, amountRequest, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Forbidden", body.get("error"));
        assertEquals("Access denied", body.get("message"));
        verify(transactionService, times(1)).withdrawFunds(accountId, BigDecimal.valueOf(50.00), userId);
    }

    // --- Get Transaction History Tests ---

    @Test
    void getTransactionHistory_Success() {
        UUID accountId = UUID.randomUUID();
        TransactionHistoryResponse historyResponse = new TransactionHistoryResponse();
        historyResponse.setTransactions(Collections.emptyList());

        when(transactionService.getTransactionHistory(accountId, userId)).thenReturn(historyResponse);

        ResponseEntity<?> response = transactionController.getTransactionHistory(accountId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(historyResponse, response.getBody());
        verify(transactionService, times(1)).getTransactionHistory(accountId, userId);
    }

    @Test
    void getTransactionHistory_Forbidden() {
        UUID accountId = UUID.randomUUID();

        when(transactionService.getTransactionHistory(accountId, userId))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));

        ResponseEntity<?> response = transactionController.getTransactionHistory(accountId, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Forbidden", body.get("error"));
        assertEquals("Access denied", body.get("message"));
        verify(transactionService, times(1)).getTransactionHistory(accountId, userId);
    }

    @Test
    void getTransactionHistory_NotFound() {
        UUID accountId = UUID.randomUUID();

        when(transactionService.getTransactionHistory(accountId, userId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        ResponseEntity<?> response = transactionController.getTransactionHistory(accountId, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Not Found", body.get("error"));
        assertEquals("Account not found", body.get("message"));
        verify(transactionService, times(1)).getTransactionHistory(accountId, userId);
    }
}