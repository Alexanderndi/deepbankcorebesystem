package com.ndifreke.core_banking_api.controller;

import com.ndifreke.core_banking_api.account.AccountService;
import com.ndifreke.core_banking_api.dto.savings.FixedDepositRequest;
import com.ndifreke.core_banking_api.dto.savings.FixedDepositResponse;
import com.ndifreke.core_banking_api.service.savings.FixedDepositService;
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
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FixedDepositControllerTest {

    @InjectMocks
    private FixedDepositController fixedDepositController;

    @Mock
    private FixedDepositService fixedDepositService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AccountService accountService;

    @Mock
    private HttpServletRequest request;

    private UUID userId;
    private UUID depositId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        depositId = UUID.randomUUID();

        when(jwtUtil.getTokenFromRequest(request)).thenReturn("mocked-token");
        when(jwtUtil.extractUserId("mocked-token")).thenReturn(userId);
    }

    // --- Create Fixed Deposit Tests ---

    @Test
    void createFixedDeposit_Success() {
        FixedDepositRequest fixedDepositRequest = new FixedDepositRequest();
        fixedDepositRequest.setDepositAmount(BigDecimal.valueOf(5000.00));
        fixedDepositRequest.setDepositDate(LocalDate.now());
        fixedDepositRequest.setMaturityDate(LocalDate.now().plusMonths(6));
        fixedDepositRequest.setInterestRate(BigDecimal.valueOf(0.03));

        FixedDepositResponse response = new FixedDepositResponse();
        response.setDepositId(depositId);
        response.setDepositAmount(BigDecimal.valueOf(5000.00));

        when(fixedDepositService.createFixedDeposit(fixedDepositRequest, userId)).thenReturn(response);

        ResponseEntity<?> result = fixedDepositController.createFixedDeposit(fixedDepositRequest, request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(fixedDepositService, times(1)).createFixedDeposit(fixedDepositRequest, userId);
    }

    @Test
    void createFixedDeposit_NoSavingsAccount() {
        FixedDepositRequest fixedDepositRequest = new FixedDepositRequest();
        fixedDepositRequest.setDepositAmount(BigDecimal.valueOf(5000.00));
        fixedDepositRequest.setDepositDate(LocalDate.now());
        fixedDepositRequest.setMaturityDate(LocalDate.now().plusMonths(6));
        fixedDepositRequest.setInterestRate(BigDecimal.valueOf(0.03));

        when(fixedDepositService.createFixedDeposit(fixedDepositRequest, userId))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "User must have a SAVINGS account to create a fixed deposit"));

        ResponseEntity<?> result = fixedDepositController.createFixedDeposit(fixedDepositRequest, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Bad Request", body.get("error"));
        assertEquals("User must have a SAVINGS account to create a fixed deposit", body.get("message"));
        verify(fixedDepositService, times(1)).createFixedDeposit(fixedDepositRequest, userId);
    }

    @Test
    void createFixedDeposit_InvalidDepositDate() {
        FixedDepositRequest fixedDepositRequest = new FixedDepositRequest();
        fixedDepositRequest.setDepositAmount(BigDecimal.valueOf(5000.00));
        fixedDepositRequest.setDepositDate(LocalDate.now().minusDays(1)); // Before today
        fixedDepositRequest.setMaturityDate(LocalDate.now().plusMonths(6));
        fixedDepositRequest.setInterestRate(BigDecimal.valueOf(0.03));

        when(fixedDepositService.createFixedDeposit(fixedDepositRequest, userId))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deposit date cannot be earlier than today"));

        ResponseEntity<?> result = fixedDepositController.createFixedDeposit(fixedDepositRequest, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Deposit date cannot be earlier than today", body.get("message"));
        verify(fixedDepositService, times(1)).createFixedDeposit(fixedDepositRequest, userId);
    }

    @Test
    void createFixedDeposit_InvalidAmount() {
        FixedDepositRequest fixedDepositRequest = new FixedDepositRequest();
        fixedDepositRequest.setDepositAmount(BigDecimal.ZERO); // Invalid
        fixedDepositRequest.setDepositDate(LocalDate.now());
        fixedDepositRequest.setMaturityDate(LocalDate.now().plusMonths(6));
        fixedDepositRequest.setInterestRate(BigDecimal.valueOf(0.03));

        when(fixedDepositService.createFixedDeposit(fixedDepositRequest, userId))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deposit amount must be greater than zero"));

        ResponseEntity<?> result = fixedDepositController.createFixedDeposit(fixedDepositRequest, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Deposit amount must be greater than zero", body.get("message"));
        verify(fixedDepositService, times(1)).createFixedDeposit(fixedDepositRequest, userId);
    }

    // --- Get Fixed Deposits Tests ---

    @Test
    void getFixedDeposits_Success() {
        FixedDepositResponse response = new FixedDepositResponse();
        response.setDepositId(depositId);
        response.setDepositAmount(BigDecimal.valueOf(5000.00));

        when(fixedDepositService.getFixedDeposits(userId)).thenReturn(Collections.singletonList(response));

        ResponseEntity<?> result = fixedDepositController.getFixedDeposits(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(Collections.singletonList(response), result.getBody());
        verify(fixedDepositService, times(1)).getFixedDeposits(userId);
    }

    @Test
    void getFixedDeposits_EmptyList() {
        when(fixedDepositService.getFixedDeposits(userId)).thenReturn(Collections.emptyList());

        ResponseEntity<?> result = fixedDepositController.getFixedDeposits(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(Collections.emptyList(), result.getBody());
        verify(fixedDepositService, times(1)).getFixedDeposits(userId);
    }

    // --- Get Fixed Deposit By ID Tests ---

    @Test
    void getFixedDepositById_Success() {
        FixedDepositResponse response = new FixedDepositResponse();
        response.setDepositId(depositId);
        response.setDepositAmount(BigDecimal.valueOf(5000.00));

        when(fixedDepositService.getFixedDepositById(depositId, userId)).thenReturn(response);

        ResponseEntity<?> result = fixedDepositController.getFixedDepositById(depositId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(fixedDepositService, times(1)).getFixedDepositById(depositId, userId);
    }

    @Test
    void getFixedDepositById_NotFound() {
        when(fixedDepositService.getFixedDepositById(depositId, userId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Fixed deposit not found"));

        ResponseEntity<?> result = fixedDepositController.getFixedDepositById(depositId, request);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Not Found", body.get("error"));
        assertEquals("Fixed deposit not found", body.get("message"));
        verify(fixedDepositService, times(1)).getFixedDepositById(depositId, userId);
    }

    @Test
    void getFixedDepositById_Forbidden() {
        when(fixedDepositService.getFixedDepositById(depositId, userId))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));

        ResponseEntity<?> result = fixedDepositController.getFixedDepositById(depositId, request);

        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Forbidden", body.get("error"));
        assertEquals("Access denied", body.get("message"));
        verify(fixedDepositService, times(1)).getFixedDepositById(depositId, userId);
    }

    // --- Withdraw Fixed Deposit Tests ---

    @Test
    void withdrawFixedDeposit_Success() {
        FixedDepositResponse response = new FixedDepositResponse();
        response.setDepositId(depositId);
        response.setStatus("CLOSED");

        when(fixedDepositService.withdrawFixedDeposit(depositId, userId)).thenReturn(response);

        ResponseEntity<?> result = fixedDepositController.withdrawFixedDeposit(depositId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(fixedDepositService, times(1)).withdrawFixedDeposit(depositId, userId);
    }

    @Test
    void withdrawFixedDeposit_NoSavingsAccount() {
        when(fixedDepositService.withdrawFixedDeposit(depositId, userId))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "User does not have a SAVINGS account for withdrawal"));

        ResponseEntity<?> result = fixedDepositController.withdrawFixedDeposit(depositId, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Bad Request", body.get("error"));
        assertEquals("User does not have a SAVINGS account for withdrawal", body.get("message"));
        verify(fixedDepositService, times(1)).withdrawFixedDeposit(depositId, userId);
    }

    @Test
    void withdrawFixedDeposit_NotMatured() {
        when(fixedDepositService.withdrawFixedDeposit(depositId, userId))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Fixed deposit not matured"));

        ResponseEntity<?> result = fixedDepositController.withdrawFixedDeposit(depositId, request);

        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Conflict", body.get("error"));
        assertEquals("Fixed deposit not matured", body.get("message"));
        verify(fixedDepositService, times(1)).withdrawFixedDeposit(depositId, userId);
    }

    @Test
    void withdrawFixedDeposit_NotFound() {
        when(fixedDepositService.withdrawFixedDeposit(depositId, userId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Fixed deposit not found"));

        ResponseEntity<?> result = fixedDepositController.withdrawFixedDeposit(depositId, request);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Not Found", body.get("error"));
        assertEquals("Fixed deposit not found", body.get("message"));
        verify(fixedDepositService, times(1)).withdrawFixedDeposit(depositId, userId);
    }

    @Test
    void withdrawFixedDeposit_Forbidden() {
        when(fixedDepositService.withdrawFixedDeposit(depositId, userId))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));

        ResponseEntity<?> result = fixedDepositController.withdrawFixedDeposit(depositId, request);

        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Forbidden", body.get("error"));
        assertEquals("Access denied", body.get("message"));
        verify(fixedDepositService, times(1)).withdrawFixedDeposit(depositId, userId);
    }
}