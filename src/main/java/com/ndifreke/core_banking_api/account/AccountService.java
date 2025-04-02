package com.ndifreke.core_banking_api.account;
import com.ndifreke.core_banking_api.entity.Account;
import com.ndifreke.core_banking_api.repository.AccountRepository;
import com.ndifreke.core_banking_api.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The type Account service.
 */
@Service
public class AccountService {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Autowired
    private final AccountRepository accountRepository;

    /**
     * Instantiates a new Account service.
     *
     * @param accountRepository the account repository
     */
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Create account account.
     *
     * @param account             the account
     * @param initialBalance      the initial balance
     * @param authenticatedUserId the authenticated user id
     * @return the account
     */
    @CachePut(value = "accounts", key = "#result.accountId")
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

    /**
     * Gets account by id.
     *
     * @param accountId           the account id
     * @param authenticatedUserId the authenticated user id
     * @return the account by id
     */
    @Cacheable(value = "accounts", key = "#accountId")
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

    /**
     * Gets accounts by user id.
     *
     * @param userId              the user id
     * @param authenticatedUserId the authenticated user id
     * @return the accounts by user id
     */
    @Cacheable(value = "accounts", key = "#userId")
    public List<Account> getAccountsByUserId(UUID userId, UUID authenticatedUserId) {
        if (!userId.equals(authenticatedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return accountRepository.findByUserId(userId);
    }

    /**
     * Gets account balance.
     *
     * @param accountId           the account id
     * @param authenticatedUserId the authenticated user id
     * @return the account balance
     */
    public BigDecimal getAccountBalance(UUID accountId, UUID authenticatedUserId) {
        Account account = getAccountById(accountId, authenticatedUserId);
        return account.getBalance();
    }

    /**
     * Update account account.
     *
     * @param updatedAccount the updated account
     * @return the account
     */
    @CachePut(value = "accounts", key = "#updatedAccount.accountId")
    public Account updateAccount(Account updatedAccount) {
        return accountRepository.save(updatedAccount);
    }

    /**
     * Delete account.
     *
     * @param accountId           the account id
     * @param authenticatedUserId the authenticated user id
     */
    @CacheEvict(value = "accounts", key = "#accountId")
    public void deleteAccount(UUID accountId, UUID authenticatedUserId) {
        Account account = getAccountById(accountId, authenticatedUserId);
        accountRepository.delete(account);
    }

    /**
     * Validate account ownership.
     *
     * @param accountId           the account id
     * @param authenticatedUserId the authenticated user id
     */
    public void validateAccountOwnership(UUID accountId, UUID authenticatedUserId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        if (!account.getUserId().equals(authenticatedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    /**
     * Find account by id optional.
     *
     * @param accountId the account id
     * @return the optional
     */
    public Optional<Account> findAccountById(UUID accountId) {
        return accountRepository.findById(accountId);
    }
}