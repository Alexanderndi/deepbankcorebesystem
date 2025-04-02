package com.ndifreke.core_banking_api.controller;

import com.ndifreke.core_banking_api.account.AccountService;
import com.ndifreke.core_banking_api.dto.account.AccountRequest;
import com.ndifreke.core_banking_api.entity.Account;
import com.ndifreke.core_banking_api.exception.NotFoundException;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @InjectMocks
    private AccountController accountController;

    @Mock
    private AccountService accountService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private JwtUtil jwtUtil;

    private UUID userId;
    private UUID accountId;
    private Account account;
    private AccountRequest accountRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        account = new Account();
        account.setAccountId(accountId);
        account.setUserId(userId);
        account.setAccountType("SAVINGS");
        account.setBalance(BigDecimal.valueOf(1000.00));

        // Configure JWT mocks
        when(jwtUtil.getTokenFromRequest(request)).thenReturn("mocked-token");
        when(jwtUtil.extractUserId("mocked-token")).thenReturn(userId);

        accountRequest = new AccountRequest();
        accountRequest.setUserId(userId);
        accountRequest.setAccountType("SAVINGS");
        accountRequest.setInitialBalance(BigDecimal.valueOf(1000.00));
    }

    // POST /api/accounts
    @Test
    void createAccount_Success() {
        when(accountService.createAccount(any(Account.class), eq(accountRequest.getInitialBalance()), eq(userId)))
                .thenReturn(account);

        ResponseEntity<Account> response = accountController.createAccount(accountRequest, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(account, response.getBody());
        verify(accountService, times(1))
                .createAccount(any(Account.class), eq(accountRequest.getInitialBalance()), eq(userId));
    }

    @Test
    void createAccount_Forbidden() {
        UUID differentUserId = UUID.randomUUID();
        accountRequest.setUserId(differentUserId); // Request has different userId
        when(accountService.createAccount(any(Account.class), eq(accountRequest.getInitialBalance()), eq(userId)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot create account for another user"));

        ResponseEntity<Account> response = accountController.createAccount(accountRequest, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
        verify(accountService, times(1))
                .createAccount(any(Account.class), eq(accountRequest.getInitialBalance()), eq(userId));
    }

    @Test
    void createAccount_AccountAlreadyExists() {
        when(accountService.createAccount(any(Account.class), eq(accountRequest.getInitialBalance()), eq(userId)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account already exists"));

        ResponseEntity<Account> response = accountController.createAccount(accountRequest, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(accountService, times(1))
                .createAccount(any(Account.class), eq(accountRequest.getInitialBalance()), eq(userId));
    }

    // GET /api/accounts/{accountId}
    @Test
    void getAccountById_Success() {
        when(accountService.getAccountById(accountId, userId)).thenReturn(account);

        ResponseEntity<Account> response = accountController.getAccountById(accountId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(account, response.getBody());
        verify(accountService, times(1)).getAccountById(accountId, userId);
    }

    @Test
    void getAccountById_NotFound() {
        when(accountService.getAccountById(accountId, userId)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> accountController.getAccountById(accountId, request));
        verify(accountService, times(1)).getAccountById(accountId, userId);
    }

    @Test
    void getAccountById_Forbidden() {
        UUID differentUserId = UUID.randomUUID();
        when(jwtUtil.extractUserId("mocked-token")).thenReturn(differentUserId); // Simulate different user
        when(accountService.getAccountById(accountId, differentUserId))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                accountController.getAccountById(accountId, request));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("403 FORBIDDEN \"Access denied\"", exception.getMessage());
        verify(accountService, times(1)).getAccountById(accountId, differentUserId);
    }

    // GET /api/accounts/user/{userId}
    @Test
    void getAccountsByUserId_Success() {
        when(accountService.getAccountsByUserId(userId, userId)).thenReturn(List.of(account));

        ResponseEntity<List<Account>> response = accountController.getAccountsByUserId(userId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(account), response.getBody());
        verify(accountService, times(1)).getAccountsByUserId(userId, userId);
    }

    @Test
    void getAccountsByUserId_Forbidden() {
        UUID differentUserId = UUID.randomUUID();
        when(jwtUtil.extractUserId("mocked-token")).thenReturn(differentUserId); // Simulate different user
        when(accountService.getAccountsByUserId(userId, differentUserId))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));

        ResponseEntity<List<Account>> response = accountController.getAccountsByUserId(userId, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
        verify(accountService, times(1)).getAccountsByUserId(userId, differentUserId);
    }

    // GET /api/accounts/{accountId}/balance
    @Test
    void getAccountBalance_Success() {
        when(accountService.getAccountBalance(accountId, userId)).thenReturn(account.getBalance());

        ResponseEntity<BigDecimal> response = accountController.getAccountBalance(accountId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(account.getBalance(), response.getBody());
        verify(accountService, times(1)).getAccountBalance(accountId, userId);
    }

    @Test
    void getAccountBalance_NotFound() {
        when(accountService.getAccountBalance(accountId, userId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        ResponseEntity<BigDecimal> response = accountController.getAccountBalance(accountId, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(accountService, times(1)).getAccountBalance(accountId, userId);
    }

    @Test
    void getAccountBalance_Forbidden() {
        UUID differentUserId = UUID.randomUUID();
        when(jwtUtil.extractUserId("mocked-token")).thenReturn(differentUserId); // Simulate different user
        when(accountService.getAccountBalance(accountId, differentUserId))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));

        ResponseEntity<BigDecimal> response = accountController.getAccountBalance(accountId, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
        verify(accountService, times(1)).getAccountBalance(accountId, differentUserId);
    }

    // DELETE /api/accounts/{accountId}
    @Test
    void deleteAccount_Success() {
        doNothing().when(accountService).deleteAccount(accountId, userId);

        ResponseEntity<Void> response = accountController.deleteAccount(accountId, request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(accountService, times(1)).deleteAccount(accountId, userId);
    }

    @Test
    void deleteAccount_NotFound() {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"))
                .when(accountService).deleteAccount(accountId, userId);

        ResponseEntity<Void> response = accountController.deleteAccount(accountId, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(accountService, times(1)).deleteAccount(accountId, userId);
    }

    @Test
    void deleteAccount_Forbidden() {
        UUID differentUserId = UUID.randomUUID();
        when(jwtUtil.extractUserId("mocked-token")).thenReturn(differentUserId); // Simulate different user
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"))
                .when(accountService).deleteAccount(accountId, differentUserId);

        ResponseEntity<Void> response = accountController.deleteAccount(accountId, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(accountService, times(1)).deleteAccount(accountId, differentUserId);
    }
}