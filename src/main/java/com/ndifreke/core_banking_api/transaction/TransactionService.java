package com.ndifreke.core_banking_api.transaction;

import com.ndifreke.core_banking_api.account.Account;
import com.ndifreke.core_banking_api.account.AccountService;
import com.ndifreke.core_banking_api.transaction.transactionType.Deposit;
import com.ndifreke.core_banking_api.transaction.transactionType.Withdrawal;
import com.ndifreke.core_banking_api.transaction.transactionType.repository.DepositRepository;
import com.ndifreke.core_banking_api.transaction.transactionType.Transfer;
import com.ndifreke.core_banking_api.transaction.transactionType.repository.TransferRepository;
import com.ndifreke.core_banking_api.transaction.transactionType.repository.WithdrawalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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

    @Transactional
    public Transfer transferFunds(UUID fromAccountId, UUID toAccountId, BigDecimal amount, String description, UUID authenticatedUserId) {
        validateAmount(amount, "transfer");
        logger.info("Transfer request: fromAccountId={}, toAccountId={}, amount={}, description={}, authenticatedUserId={}",
                fromAccountId, toAccountId, amount, description, authenticatedUserId);

        Account fromAccount = accountService.getAccountById(fromAccountId, authenticatedUserId);
        Account toAccount = accountService.findAccountById(toAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination account not found"));

        if(fromAccountId == null || toAccountId == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromAccountId and toAccountId must not be null for transfer");
        }

        logger.info("From Account User ID: {}, To Account User ID: {}", fromAccount.getUserId(), toAccount.getUserId());

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }

        BigDecimal newFromBalance = fromAccount.getBalance().subtract(amount);
        BigDecimal newToBalance = toAccount.getBalance().add(amount);

        fromAccount.setBalance(newFromBalance);
        toAccount.setBalance(newToBalance);

        accountService.updateAccount(fromAccount);
        accountService.updateAccount(toAccount);

        Transfer transaction = new Transfer();
        transaction.setFromAccountId(fromAccountId);
        transaction.setToAccountId(toAccountId);
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setDescription(description);
        transferRepository.save(transaction);

        logger.info("Transfer successful: fromAccountId={}, toAccountId={}, amount={}, description={}",
                fromAccountId, toAccountId, amount, description);
        return transaction;
    }

    @Transactional
    public Deposit depositFunds(UUID accountId, BigDecimal amount, UUID authenticatedUserId) {
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
        deposit.setTransactionType(TransactionType.DEPOSIT);
        depositRepository.save(deposit);

        logger.info("Deposit successful: accountId={}, amount={}", accountId, amount);
        return deposit;
    }

    @Transactional
    public Withdrawal withdrawFunds(UUID accountId, BigDecimal amount, UUID authenticatedUserId) {
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

        logger.info("Withdrawal successful: accountId={}, amount={}", accountId, amount);
        return withdrawal;
    }

    public List<Transfer> getTransactionHistory(UUID accountId, UUID authenticatedUserId) {
        accountService.validateAccountOwnership(accountId, authenticatedUserId);
        return transferRepository.findByFromAccountIdOrToAccountIdOrderByTransactionDateDesc(accountId, accountId);
    }

    private void validateAmount(BigDecimal amount, String operation) {
        if (amount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount cannot be null for " + operation);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero for " + operation);
        }
    }
}