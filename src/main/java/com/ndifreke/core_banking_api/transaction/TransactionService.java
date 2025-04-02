package com.ndifreke.core_banking_api.transaction;

import com.ndifreke.core_banking_api.account.Account;
import com.ndifreke.core_banking_api.account.AccountService;
import com.ndifreke.core_banking_api.notification.MailService;
import com.ndifreke.core_banking_api.security.fraud_detection.FraudRules;
import com.ndifreke.core_banking_api.transaction.response.*;
import com.ndifreke.core_banking_api.transaction.transactionType.Deposit;
import com.ndifreke.core_banking_api.transaction.transactionType.Transfer;
import com.ndifreke.core_banking_api.transaction.transactionType.Withdrawal;
import com.ndifreke.core_banking_api.transaction.transactionType.repository.DepositRepository;
import com.ndifreke.core_banking_api.transaction.transactionType.repository.TransferRepository;
import com.ndifreke.core_banking_api.transaction.transactionType.repository.WithdrawalRepository;
import com.ndifreke.core_banking_api.user.User;
import com.ndifreke.core_banking_api.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type Transaction service.
 */
@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private AccountService accountService;

    @Autowired
    private DepositRepository depositRepository;

    @Autowired
    private WithdrawalRepository withdrawalRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private UserRepository userRepository;

    /**
     * Transfer funds transfer response.
     *
     * @param fromAccountId       the from account id
     * @param toAccountId         the to account id
     * @param amount              the amount
     * @param description         the description
     * @param authenticatedUserId the authenticated user id
     * @return the transfer response
     */
    @Transactional
    public TransferResponse transferFunds(UUID fromAccountId, UUID toAccountId, BigDecimal amount, String description, UUID authenticatedUserId) {
        validateAmount(amount, "transfer");
        logger.info("Transfer request: fromAccountId={}, toAccountId={}, amount={}, description={}, authenticatedUserId={}",
                fromAccountId, toAccountId, amount, description, authenticatedUserId);

        Account fromAccount = accountService.getAccountById(fromAccountId, authenticatedUserId);
        Account toAccount = accountService.findAccountById(toAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination account not found"));

        if(fromAccountId == null || toAccountId == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromAccountId and toAccountId must not be null for transfer");
        }

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }

        // Check for fraud
        String fraudReason = isFraudulentTransfer(fromAccount, toAccount, amount);
        if (fraudReason != null) {
            User fromUser = userRepository.findById(fromAccount.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            // Send fraud alert email
            if ("Large transfer amount".equals(fraudReason)) {
                mailService.sendFraudAlertEmail(fromUser.getEmail(), fraudReason, amount);
            } else {
                mailService.sendFraudAlertEmail(fromUser.getEmail(), fraudReason, null);
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Transaction blocked due to potential fraud: " + fraudReason);
        }


        BigDecimal newFromBalance = fromAccount.getBalance().subtract(amount);
        BigDecimal newToBalance = toAccount.getBalance().add(amount);

        fromAccount.setBalance(newFromBalance);
        toAccount.setBalance(newToBalance);

        accountService.updateAccount(fromAccount);
        accountService.updateAccount(toAccount);

        Transfer transfer = new Transfer();
        transfer.setFromAccountId(fromAccountId);
        transfer.setToAccountId(toAccountId);
        transfer.setAmount(amount);
        transfer.setTransactionDate(new Date());
        transfer.setTransactionType(TransactionType.TRANSFER);
        transfer.setDescription(description);
        transferRepository.save(transfer);

        Cache cache = cacheManager.getCache("transactionTimestamps");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(fromAccount.getAccountNumber());
            List<LocalDateTime> timestamps = (wrapper != null) ? (List<LocalDateTime>) wrapper.get() : new ArrayList<>();
            assert timestamps != null;
            timestamps.add(LocalDateTime.now());
            cache.put(fromAccount.getAccountNumber(), timestamps);
        }

        // Send success emails
        User fromUser = userRepository.findById(fromAccount.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender user not found"));
        User toUser = userRepository.findById(toAccount.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver user not found"));

        mailService.sendTransferDebitEmail(fromUser.getEmail(), fromUser.getFirstName(), amount,
                fromAccount.getAccountNumber(), toAccount.getAccountNumber(), description);
        mailService.sendTransferCreditEmail(toUser.getEmail(), toUser.getFirstName(), amount,
                toAccount.getAccountNumber(), fromAccount.getAccountNumber(), description);

        logger.info("Transfer successful: fromAccountId={}, toAccountId={}, amount={}, description={}",
                fromAccountId, toAccountId, amount, description);
        return convertToTransferResponse(transfer);
    }

    /**
     * Deposit funds deposit response.
     *
     * @param accountId           the account id
     * @param amount              the amount
     * @param authenticatedUserId the authenticated user id
     * @return the deposit response
     */
    @Transactional
    public DepositResponse depositFunds(UUID accountId, BigDecimal amount, UUID authenticatedUserId) {
        validateAmount(amount, "deposit");
        logger.info("Deposit request: accountId={}, amount={}, authenticatedUserId={}",
                accountId, amount, authenticatedUserId);

        Account account = accountService.getAccountById(accountId, authenticatedUserId);

        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);

        accountService.updateAccount(account);

        Deposit deposit = new Deposit();
        deposit.setAccountId(accountId);
        deposit.setAmount(amount);
        deposit.setTransactionDate(new Date());
        deposit.setTransactionType(TransactionType.DEPOSIT);
        depositRepository.save(deposit);

        // Send Email Notification
        User user = userRepository.findById(account.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        mailService.sendDepositEmail(user.getEmail(), user.getFirstName(), amount, account.getAccountNumber());

        logger.info("Deposit successful: accountId={}, amount={}", accountId, amount);
        return convertToDepositResponse(deposit);
    }

    /**
     * Withdraw funds withdrawal response.
     *
     * @param accountId           the account id
     * @param amount              the amount
     * @param authenticatedUserId the authenticated user id
     * @return the withdrawal response
     */
    @Transactional
    public WithdrawalResponse withdrawFunds(UUID accountId, BigDecimal amount, UUID authenticatedUserId) {
        validateAmount(amount, "withdrawal");
        logger.info("Withdrawal request: accountId={}, amount={}, authenticatedUserId={}",
                accountId, amount, authenticatedUserId);

        Account account = accountService.getAccountById(accountId, authenticatedUserId);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);

        accountService.updateAccount(account);

        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setAccountId(accountId);
        withdrawal.setAmount(amount);
        withdrawal.setTransactionType(TransactionType.WITHDRAWAL);
        withdrawalRepository.save(withdrawal);

        // Send Email Notification
        User user = userRepository.findById(account.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        mailService.sendWithdrawalEmail(user.getEmail(), user.getFirstName(), amount, account.getAccountNumber());

        logger.info("Withdrawal successful: accountId={}, amount={}", accountId, amount);
        return convertToWithdrawalResponse(withdrawal);
    }

    /**
     * Gets transaction history.
     *
     * @param accountId           the account id
     * @param authenticatedUserId the authenticated user id
     * @return the transaction history
     */
    public TransactionHistoryResponse getTransactionHistory(UUID accountId, UUID authenticatedUserId) {
        accountService.validateAccountOwnership(accountId, authenticatedUserId);

        List<TransferResponse> transfers = transferRepository.findByFromAccountIdOrToAccountIdOrderByTransactionDateDesc(accountId, accountId).stream()
                .map(this::convertToTransferResponse).toList();
        List<DepositResponse> deposits = depositRepository.findByAccountIdOrderByTransactionDateDesc(accountId).stream()
                .map(this::convertToDepositResponse).toList();
        List<WithdrawalResponse> withdrawals = withdrawalRepository.findByAccountIdOrderByTransactionDateDesc(accountId).stream()
                .map(this::convertToWithdrawalResponse).toList();

        List<TransactionResponseInterface> transactionResponses = Stream.of(
                        (List<TransactionResponseInterface>) (List<?>) transfers,
                        (List<TransactionResponseInterface>) (List<?>) deposits,
                        (List<TransactionResponseInterface>) (List<?>) withdrawals
                )
                .flatMap(List::stream)
                .sorted(Comparator.comparing(this::getTransactionDate).reversed())
                .toList();

        TransactionHistoryResponse response = new TransactionHistoryResponse();
        response.setTransactions(transactionResponses);
        return response;
    }

    private Date getTransactionDate(TransactionResponseInterface transaction) {
        if (transaction instanceof TransferResponse transfer) {
            return transfer.getTransactionDate();
        } else if (transaction instanceof DepositResponse deposit) {
            return deposit.getTransactionDate();
        } else if (transaction instanceof WithdrawalResponse withdrawal) {
            return withdrawal.getTransactionDate();
        }
        return null;
    }

    private void validateAmount(BigDecimal amount, String operation) {
        if (amount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount cannot be null for " + operation);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero for " + operation);
        }
    }

    private TransferResponse convertToTransferResponse(Transfer transfer) {
        return TransactionService.getTransferResponse(transfer);
    }

    private TransferResponse convertToTransactionResponse(Transfer transaction) {
        return TransactionService.getTransferResponse(transaction);
    }

    /**
     * Gets transfer response.
     *
     * @param transaction the transaction
     * @return the transfer response
     */
    static TransferResponse getTransferResponse(Transfer transaction) {
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
        return TransactionService.getDepositResponse(deposit);
    }

    /**
     * Gets deposit response.
     *
     * @param deposit the deposit
     * @return the deposit response
     */
    static DepositResponse getDepositResponse(Deposit deposit) {
        DepositResponse response = new DepositResponse();
        response.setDepositId(deposit.getDepositId());
        response.setAccountId(deposit.getAccountId());
        response.setAmount(deposit.getAmount());
        response.setTransactionDate(deposit.getTransactionDate());
        response.setTransactionType(deposit.getTransactionType());
        return response;
    }

    private WithdrawalResponse convertToWithdrawalResponse(Withdrawal withdrawal) {
        return TransactionService.getWithdrawalResponse(withdrawal);
    }

    /**
     * Gets withdrawal response.
     *
     * @param withdrawal the withdrawal
     * @return the withdrawal response
     */
    static WithdrawalResponse getWithdrawalResponse(Withdrawal withdrawal) {
        WithdrawalResponse response = new WithdrawalResponse();
        response.setWithdrawalId(withdrawal.getWithdrawalId());
        response.setAccountId(withdrawal.getAccountId());
        response.setAmount(withdrawal.getAmount());
        response.setTransactionDate(withdrawal.getTransactionDate());
        response.setTransactionType(withdrawal.getTransactionType());
        return response;
    }

    private String isFraudulentTransfer(Account fromAccount, Account toAccount, BigDecimal amount) {
        if (amount.compareTo(FraudRules.LARGE_TRANSFER_THRESHOLD) > 0) {
            logger.warn("Potential fraud: Large transfer amount detected: {}", amount);
            return "Large transfer amount";
        }
        if (isAccountBlacklisted(fromAccount.getAccountNumber()) || isAccountBlacklisted(toAccount.getAccountNumber())) {
            logger.warn("Potential fraud: Transaction with blacklisted account");
            return "Blacklisted account";
        }
        if (isHighFrequencyTransaction(fromAccount.getAccountNumber())) {
            logger.warn("Potential fraud: High frequency transaction from account: {}", fromAccount.getAccountNumber());
            return "High frequency transaction";
        }
        return null;
    }

    private boolean isAccountBlacklisted(String accountNumber) {
        return false;
    }

    private boolean isHighFrequencyTransaction(String accountNumber) {
        // Get the cache instance for transaction timestamps
        Cache cache = cacheManager.getCache("transactionTimestamps");
        if (cache == null) {
            return false;
        }

        // Retrieve the list of timestamps from the cache for this account
        Cache.ValueWrapper wrapper = cache.get(accountNumber);
        List<LocalDateTime> timestamps = (wrapper != null) ? (List<LocalDateTime>) wrapper.get() : null;

        // If no timestamps exist or the list is empty, no high-frequency transactions
        if (timestamps == null || timestamps.isEmpty()) {
            return false;
        }

        // Calculate the time window (10 minutes ago from now)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenMinutesAgo = now.minusMinutes(FraudRules.HIGH_FREQUENCY_TIMEFRAME_MINUTES);

        // Filter timestamps to keep only those within the last 10 minutes
        List<LocalDateTime> recentTimestamps = timestamps.stream()
                .filter(t -> t.isAfter(tenMinutesAgo))
                .collect(Collectors.toList());

        // Update the cache with the filtered list to remove old timestamps
        cache.put(accountNumber, recentTimestamps);

        // Check if the number of recent transactions meets or exceeds the limit (5)
        return recentTimestamps.size() >= FraudRules.HIGH_FREQUENCY_TRANSACTION_LIMIT;
    }
}