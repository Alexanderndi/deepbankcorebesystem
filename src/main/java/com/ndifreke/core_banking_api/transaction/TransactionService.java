package com.ndifreke.core_banking_api.transaction;

import com.ndifreke.core_banking_api.account.Account;
import com.ndifreke.core_banking_api.account.AccountService;
import com.ndifreke.core_banking_api.transaction.transactionType.Deposit;
import com.ndifreke.core_banking_api.transaction.transactionType.Transfer;
import com.ndifreke.core_banking_api.transaction.transactionType.Withdrawal;
import com.ndifreke.core_banking_api.transaction.transactionType.repository.DepositRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private DepositRepository depositRepository;

    @Autowired
    private WithdrawalRepository withdrawalRepository;

    @Transactional
    public void transferFunds(UUID fromAccountId, UUID toAccountId, BigDecimal amount, UUID authenticatedUserId) {
        logger.info("Transfer request: fromAccountId={}, toAccountId={}, amount={}, authenticatedUserId={}",
                fromAccountId, toAccountId, amount, authenticatedUserId);

        Account fromAccount = accountService.getAccountById(fromAccountId, authenticatedUserId);
        Account toAccount = accountService.findAccountById(toAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination account not found"));

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

        Transfer transfer = new Transfer();
        transfer.setFromAccountId(fromAccountId);
        transfer.setToAccountId(toAccountId);
        transfer.setAmount(amount);
        transferRepository.save(transfer);

        logger.info("Transfer successful: fromAccountId={}, toAccountId={}, amount={}",
                fromAccountId, toAccountId, amount);
    }

    @Transactional
    public void depositFunds(UUID accountId, BigDecimal amount, UUID authenticatedUserId) {
        logger.info("Deposit request: accountId={}, amount={}, authenticatedUserId={}",
                accountId, amount, authenticatedUserId);

        Account account = accountService.getAccountById(accountId, authenticatedUserId);

        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);

        accountService.updateAccount(account);

        Deposit deposit = new Deposit();
        deposit.setAccountId(accountId);
        deposit.setAmount(amount);
        depositRepository.save(deposit);

        logger.info("Deposit successful: accountId={}, amount={}", accountId, amount);
    }

    @Transactional
    public void withdrawFunds(UUID accountId, BigDecimal amount, UUID authenticatedUserId) {
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
        withdrawalRepository.save(withdrawal);

        logger.info("Withdrawal successful: accountId={}, amount={}", accountId, amount);
    }

    public List<Object> getTransactionHistory(UUID accountId, UUID authenticatedUserId) {
        accountService.validateAccountOwnership(accountId, authenticatedUserId);
        List<Object> transactions = new ArrayList<>();
        transactions.addAll(transferRepository.findByFromAccountIdOrToAccountIdOrderByTransactionDateDesc(accountId, accountId));
        transactions.addAll(depositRepository.findByAccountIdOrderByTransactionDateDesc(accountId));
        transactions.addAll(withdrawalRepository.findByAccountIdOrderByTransactionDateDesc(accountId));
        return transactions;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }
    }
}