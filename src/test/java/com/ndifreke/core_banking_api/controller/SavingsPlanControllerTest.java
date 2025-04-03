package com.ndifreke.core_banking_api.controller;

import com.ndifreke.core_banking_api.account.AccountService;
import com.ndifreke.core_banking_api.dto.savings.SavingsPlanDepositRequest;
import com.ndifreke.core_banking_api.dto.savings.SavingsPlanRequest;
import com.ndifreke.core_banking_api.dto.savings.SavingsPlanResponse;
import com.ndifreke.core_banking_api.dto.savings.SavingsPlanWithdrawalRequest;
import com.ndifreke.core_banking_api.service.savings.SavingsPlanService;
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
class SavingsPlanControllerTest {

    @InjectMocks
    private SavingsPlanController savingsPlanController;

    @Mock
    private SavingsPlanService savingsPlanService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private AccountService accountService;

    private UUID userId;
    private UUID planId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        planId = UUID.randomUUID();

        // Mock JWT behavior
        when(jwtUtil.getTokenFromRequest(request)).thenReturn("mocked-token");
        when(jwtUtil.extractUserId("mocked-token")).thenReturn(userId);
    }

    // --- Create Savings Plan Tests ---

    @Test
    void createSavingsPlan_Success() {
        SavingsPlanRequest savingsPlanRequest = new SavingsPlanRequest();
        savingsPlanRequest.setPlanName("Test Plan");
        savingsPlanRequest.setTargetAmount(BigDecimal.valueOf(1000.00));
        savingsPlanRequest.setStartDate(LocalDate.now());
        savingsPlanRequest.setEndDate(LocalDate.now().plusMonths(1));
        savingsPlanRequest.setInterestRate(BigDecimal.valueOf(0.05));
        savingsPlanRequest.setRecurringDepositAmount(BigDecimal.valueOf(100.00));
        savingsPlanRequest.setRecurringDepositFrequency("MONTHLY");

        SavingsPlanResponse response = new SavingsPlanResponse();
        response.setPlanId(planId);
        response.setPlanName("Test Plan");

        when(savingsPlanService.createSavingsPlan(savingsPlanRequest, userId)).thenReturn(response);

        ResponseEntity<?> result = savingsPlanController.createSavingsPlan(savingsPlanRequest, request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(savingsPlanService, times(1)).createSavingsPlan(savingsPlanRequest, userId);
    }

    @Test
    void createSavingsPlan_NoSavingsAccount() {
        SavingsPlanRequest savingsPlanRequest = new SavingsPlanRequest();
        savingsPlanRequest.setPlanName("Test Plan");
        savingsPlanRequest.setTargetAmount(BigDecimal.valueOf(1000.00));
        savingsPlanRequest.setStartDate(LocalDate.now());
        savingsPlanRequest.setEndDate(LocalDate.now().plusMonths(1));
        savingsPlanRequest.setInterestRate(BigDecimal.valueOf(0.05));
        savingsPlanRequest.setRecurringDepositAmount(BigDecimal.valueOf(100.00));
        savingsPlanRequest.setRecurringDepositFrequency("MONTHLY");

        when(savingsPlanService.createSavingsPlan(savingsPlanRequest, userId))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "User must have a SAVINGS account to create a savings plan"));

        ResponseEntity<?> result = savingsPlanController.createSavingsPlan(savingsPlanRequest, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Bad Request", body.get("error"));
        assertEquals("User must have a SAVINGS account to create a savings plan", body.get("message"));
        verify(savingsPlanService, times(1)).createSavingsPlan(savingsPlanRequest, userId);
    }

    @Test
    void createSavingsPlan_InvalidStartDate() {
        SavingsPlanRequest savingsPlanRequest = new SavingsPlanRequest();
        savingsPlanRequest.setPlanName("Test Plan");
        savingsPlanRequest.setTargetAmount(BigDecimal.valueOf(1000.00));
        savingsPlanRequest.setStartDate(LocalDate.now().minusDays(1)); // Before today
        savingsPlanRequest.setEndDate(LocalDate.now().plusMonths(1));
        savingsPlanRequest.setInterestRate(BigDecimal.valueOf(0.05));
        savingsPlanRequest.setRecurringDepositAmount(BigDecimal.valueOf(100.00));
        savingsPlanRequest.setRecurringDepositFrequency("MONTHLY");

        when(savingsPlanService.createSavingsPlan(savingsPlanRequest, userId))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be earlier than today"));

        ResponseEntity<?> result = savingsPlanController.createSavingsPlan(savingsPlanRequest, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Start date cannot be earlier than today", body.get("message"));
        verify(savingsPlanService, times(1)).createSavingsPlan(savingsPlanRequest, userId);
    }

    @Test
    void createSavingsPlan_InvalidRequest() {
        SavingsPlanRequest savingsPlanRequest = new SavingsPlanRequest();
        savingsPlanRequest.setPlanName("Test Plan");
        savingsPlanRequest.setTargetAmount(BigDecimal.ZERO); // Invalid
        savingsPlanRequest.setStartDate(LocalDate.now());
        savingsPlanRequest.setEndDate(LocalDate.now().plusMonths(1));

        when(savingsPlanService.createSavingsPlan(savingsPlanRequest, userId))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target amount must be greater than zero"));

        ResponseEntity<?> result = savingsPlanController.createSavingsPlan(savingsPlanRequest, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Target amount must be greater than zero", body.get("message"));
        verify(savingsPlanService, times(1)).createSavingsPlan(savingsPlanRequest, userId);
    }

    // --- Get Savings Plans Tests ---

    @Test
    void getSavingsPlans_Success() {
        SavingsPlanResponse response = new SavingsPlanResponse();
        response.setPlanId(planId);
        response.setPlanName("Test Plan");

        when(savingsPlanService.getSavingsPlans(userId)).thenReturn(Collections.singletonList(response));

        ResponseEntity<?> result = savingsPlanController.getSavingsPlans(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(Collections.singletonList(response), result.getBody());
        verify(savingsPlanService, times(1)).getSavingsPlans(userId);
    }

    @Test
    void getSavingsPlans_EmptyList() {
        when(savingsPlanService.getSavingsPlans(userId)).thenReturn(Collections.emptyList());

        ResponseEntity<?> result = savingsPlanController.getSavingsPlans(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(Collections.emptyList(), result.getBody());
        verify(savingsPlanService, times(1)).getSavingsPlans(userId);
    }

    // --- Get Savings Plan By ID Tests ---

    @Test
    void getSavingsPlanById_Success() {
        SavingsPlanResponse response = new SavingsPlanResponse();
        response.setPlanId(planId);
        response.setPlanName("Test Plan");

        when(savingsPlanService.getSavingsPlanById(planId, userId)).thenReturn(response);

        ResponseEntity<?> result = savingsPlanController.getSavingsPlanById(planId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(savingsPlanService, times(1)).getSavingsPlanById(planId, userId);
    }

    @Test
    void getSavingsPlanById_NotFound() {
        when(savingsPlanService.getSavingsPlanById(planId, userId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Savings plan not found"));

        ResponseEntity<?> result = savingsPlanController.getSavingsPlanById(planId, request);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Not Found", body.get("error"));
        assertEquals("Savings plan not found", body.get("message"));
        verify(savingsPlanService, times(1)).getSavingsPlanById(planId, userId);
    }

    @Test
    void getSavingsPlanById_Forbidden() {
        when(savingsPlanService.getSavingsPlanById(planId, userId))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));

        ResponseEntity<?> result = savingsPlanController.getSavingsPlanById(planId, request);

        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Forbidden", body.get("error"));
        assertEquals("Access denied", body.get("message"));
        verify(savingsPlanService, times(1)).getSavingsPlanById(planId, userId);
    }

    // --- Deposit to Savings Plan Tests ---

    @Test
    void depositToSavingsPlan_Success() {
        SavingsPlanDepositRequest depositRequest = new SavingsPlanDepositRequest();
        depositRequest.setAmount(BigDecimal.valueOf(50.00));

        SavingsPlanResponse response = new SavingsPlanResponse();
        response.setPlanId(planId);
        response.setCurrentBalance(BigDecimal.valueOf(50.00));

        when(savingsPlanService.depositToSavingsPlan(planId, BigDecimal.valueOf(50.00), userId)).thenReturn(response);

        ResponseEntity<?> result = savingsPlanController.depositToSavingsPlan(planId, depositRequest, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(savingsPlanService, times(1)).depositToSavingsPlan(planId, BigDecimal.valueOf(50.00), userId);
    }

    @Test
    void depositToSavingsPlan_InvalidAmount() {
        SavingsPlanDepositRequest depositRequest = new SavingsPlanDepositRequest();
        depositRequest.setAmount(BigDecimal.ZERO);

        when(savingsPlanService.depositToSavingsPlan(planId, BigDecimal.ZERO, userId))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deposit amount must be greater than zero"));

        ResponseEntity<?> result = savingsPlanController.depositToSavingsPlan(planId, depositRequest, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Deposit amount must be greater than zero", body.get("message"));
        verify(savingsPlanService, times(1)).depositToSavingsPlan(planId, BigDecimal.ZERO, userId);
    }

    @Test
    void depositToSavingsPlan_NotFound() {
        SavingsPlanDepositRequest depositRequest = new SavingsPlanDepositRequest();
        depositRequest.setAmount(BigDecimal.valueOf(50.00));

        when(savingsPlanService.depositToSavingsPlan(planId, BigDecimal.valueOf(50.00), userId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Savings plan not found"));

        ResponseEntity<?> result = savingsPlanController.depositToSavingsPlan(planId, depositRequest, request);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Not Found", body.get("error"));
        assertEquals("Savings plan not found", body.get("message"));
        verify(savingsPlanService, times(1)).depositToSavingsPlan(planId, BigDecimal.valueOf(50.00), userId);
    }

    @Test
    void depositToSavingsPlan_Forbidden() {
        SavingsPlanDepositRequest depositRequest = new SavingsPlanDepositRequest();
        depositRequest.setAmount(BigDecimal.valueOf(50.00));

        when(savingsPlanService.depositToSavingsPlan(planId, BigDecimal.valueOf(50.00), userId))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));

        ResponseEntity<?> result = savingsPlanController.depositToSavingsPlan(planId, depositRequest, request);

        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Forbidden", body.get("error"));
        assertEquals("Access denied", body.get("message"));
        verify(savingsPlanService, times(1)).depositToSavingsPlan(planId, BigDecimal.valueOf(50.00), userId);
    }

    // --- Withdraw from Savings Plan Tests ---

    @Test
    void withdrawFromSavingsPlan_Success() {
        SavingsPlanWithdrawalRequest withdrawalRequest = new SavingsPlanWithdrawalRequest();
        withdrawalRequest.setAmount(BigDecimal.valueOf(25.00));

        SavingsPlanResponse response = new SavingsPlanResponse();
        response.setPlanId(planId);
        response.setCurrentBalance(BigDecimal.valueOf(25.00));

        when(savingsPlanService.withdrawFromSavingsPlan(planId, BigDecimal.valueOf(25.00), userId)).thenReturn(response);

        ResponseEntity<?> result = savingsPlanController.withdrawFromSavingsPlan(planId, withdrawalRequest, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(savingsPlanService, times(1)).withdrawFromSavingsPlan(planId, BigDecimal.valueOf(25.00), userId);
    }

    @Test
    void withdrawFromSavingsPlan_NoSavingsAccount() {
        SavingsPlanWithdrawalRequest withdrawalRequest = new SavingsPlanWithdrawalRequest();
        withdrawalRequest.setAmount(BigDecimal.valueOf(25.00));

        when(savingsPlanService.withdrawFromSavingsPlan(planId, BigDecimal.valueOf(25.00), userId))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "User does not have a SAVINGS account for withdrawal"));

        ResponseEntity<?> result = savingsPlanController.withdrawFromSavingsPlan(planId, withdrawalRequest, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Bad Request", body.get("error"));
        assertEquals("User does not have a SAVINGS account for withdrawal", body.get("message"));
        verify(savingsPlanService, times(1)).withdrawFromSavingsPlan(planId, BigDecimal.valueOf(25.00), userId);
    }

    @Test
    void withdrawFromSavingsPlan_InvalidAmount() {
        SavingsPlanWithdrawalRequest withdrawalRequest = new SavingsPlanWithdrawalRequest();
        withdrawalRequest.setAmount(BigDecimal.ZERO);

        when(savingsPlanService.withdrawFromSavingsPlan(planId, BigDecimal.ZERO, userId))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Withdrawal amount must be greater than zero"));

        ResponseEntity<?> result = savingsPlanController.withdrawFromSavingsPlan(planId, withdrawalRequest, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Withdrawal amount must be greater than zero", body.get("message"));
        verify(savingsPlanService, times(1)).withdrawFromSavingsPlan(planId, BigDecimal.ZERO, userId);
    }

    @Test
    void withdrawFromSavingsPlan_InsufficientBalance() {
        SavingsPlanWithdrawalRequest withdrawalRequest = new SavingsPlanWithdrawalRequest();
        withdrawalRequest.setAmount(BigDecimal.valueOf(50.00));

        when(savingsPlanService.withdrawFromSavingsPlan(planId, BigDecimal.valueOf(50.00), userId))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance"));

        ResponseEntity<?> result = savingsPlanController.withdrawFromSavingsPlan(planId, withdrawalRequest, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Insufficient balance", body.get("message"));
        verify(savingsPlanService, times(1)).withdrawFromSavingsPlan(planId, BigDecimal.valueOf(50.00), userId);
    }

    @Test
    void withdrawFromSavingsPlan_NotFound() {
        SavingsPlanWithdrawalRequest withdrawalRequest = new SavingsPlanWithdrawalRequest();
        withdrawalRequest.setAmount(BigDecimal.valueOf(25.00));

        when(savingsPlanService.withdrawFromSavingsPlan(planId, BigDecimal.valueOf(25.00), userId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Savings plan not found"));

        ResponseEntity<?> result = savingsPlanController.withdrawFromSavingsPlan(planId, withdrawalRequest, request);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Not Found", body.get("error"));
        assertEquals("Savings plan not found", body.get("message"));
        verify(savingsPlanService, times(1)).withdrawFromSavingsPlan(planId, BigDecimal.valueOf(25.00), userId);
    }

    @Test
    void withdrawFromSavingsPlan_Forbidden() {
        SavingsPlanWithdrawalRequest withdrawalRequest = new SavingsPlanWithdrawalRequest();
        withdrawalRequest.setAmount(BigDecimal.valueOf(25.00));

        when(savingsPlanService.withdrawFromSavingsPlan(planId, BigDecimal.valueOf(25.00), userId))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));

        ResponseEntity<?> result = savingsPlanController.withdrawFromSavingsPlan(planId, withdrawalRequest, request);

        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Forbidden", body.get("error"));
        assertEquals("Access denied", body.get("message"));
        verify(savingsPlanService, times(1)).withdrawFromSavingsPlan(planId, BigDecimal.valueOf(25.00), userId);
    }
}