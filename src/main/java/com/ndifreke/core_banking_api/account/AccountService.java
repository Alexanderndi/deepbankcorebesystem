package com.ndifreke.core_banking_api.account;

import com.ndifreke.core_banking_api.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Autowired
    private AccountRepository accountRepository;

    public Account createAccount(Account account, BigDecimal initialBalance, UUID authenticatedUserId) {
        if (!account.getUserId().equals(authenticatedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot create account for another user.");
        }

        if (accountRepository.findByUserIdAndAccountType(account.getUserId(), account.getAccountType()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already has an account of this type.");
        }

        String accountNumber = AccountNumberGenerator.generateTimestampUUIDAccountNumber();
        account.setAccountNumber(accountNumber);
        account.setBalance(initialBalance);
        return accountRepository.save(account);
    }

    public Account getAccountById(UUID accountId, UUID authenticatedUserId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        logger.info("Account ID: {}, Authenticated User ID: {}, Account User ID: {}",
                accountId, authenticatedUserId, account.getUserId());

        if (!account.getUserId().equals(authenticatedUserId)) {
            logger.warn("Access denied: Authenticated user ID does not match account user ID.");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return account;
    }

    public List<Account> getAccountsByUserId(UUID userId, UUID authenticatedUserId) {
        if (!userId.equals(authenticatedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return accountRepository.findByUserId(userId);
    }

    public BigDecimal getAccountBalance(UUID accountId, UUID authenticatedUserId) {
        Account account = getAccountById(accountId, authenticatedUserId);
        return account.getBalance();
    }

    public Account updateAccount(Account updatedAccount) {
        return accountRepository.save(updatedAccount);
    }

    public void deleteAccount(UUID accountId, UUID authenticatedUserId) {
        Account account = getAccountById(accountId, authenticatedUserId);
        accountRepository.delete(account);
    }

    public void validateAccountOwnership(UUID accountId, UUID authenticatedUserId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        if (!account.getUserId().equals(authenticatedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    public Optional<Account> findAccountById(UUID accountId) {
        return accountRepository.findById(accountId);
    }
}