package com.ndifreke.core_banking_api.transaction.events.service;

import com.ndifreke.core_banking_api.account.Account;
import com.ndifreke.core_banking_api.account.AccountService;
import com.ndifreke.core_banking_api.transaction.TransactionType;
import com.ndifreke.core_banking_api.transaction.events.DepositEvent;
import com.ndifreke.core_banking_api.transaction.events.FundsTransferEvent;
import com.ndifreke.core_banking_api.transaction.events.WithdrawalEvent;
import com.ndifreke.core_banking_api.transaction.response.DepositResponse;
import com.ndifreke.core_banking_api.transaction.response.TransferResponse;
import com.ndifreke.core_banking_api.transaction.response.WithdrawalResponse;
import com.ndifreke.core_banking_api.transaction.transactionType.Deposit;
import com.ndifreke.core_banking_api.transaction.transactionType.Transfer;
import com.ndifreke.core_banking_api.transaction.transactionType.Withdrawal;
import com.ndifreke.core_banking_api.transaction.transactionType.repository.DepositRepository;
import com.ndifreke.core_banking_api.transaction.transactionType.repository.TransferRepository;
import com.ndifreke.core_banking_api.transaction.transactionType.repository.WithdrawalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
public class TransactionEventService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private AccountService accountService;

    @Autowired
    private DepositRepository depositRepository;

    @Autowired
    private WithdrawalRepository withdrawalRepository;

    @Autowired
    private TransferRepository transferRepository;


    @Transactional
    public TransferResponse transferFunds(UUID fromAccountId, UUID toAccountId, BigDecimal amount, String description, UUID authenticatedUserId) {
        // ... (validation logic) ...
        validateAmount(amount, "transfer");

        Account fromAccount = accountService.getAccountById(fromAccountId, authenticatedUserId);
        Account toAccount = accountService.findAccountById(toAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination account not found"));

        if(fromAccountId == null || toAccountId == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromAccountId and toAccountId must not be null for transfer");
        }

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
        transfer.setTransactionDate(new Date());
        transfer.setTransactionId(UUID.randomUUID());
        transferRepository.save(transfer);

        // ... event creation ...

        FundsTransferEvent event = new FundsTransferEvent(transfer.getTransactionId(), fromAccountId, toAccountId, amount);
        kafkaTemplate.send("funds-transfer-events", event);

        TransferResponse response = new TransferResponse();
        response.setTransactionId(transfer.getTransactionId());
        response.setFromAccountId(transfer.getFromAccountId());
        response.setToAccountId(transfer.getToAccountId());
        response.setAmount(transfer.getAmount());
        response.setTransactionDate(transfer.getTransactionDate());
        return response;
    }

    @Transactional
    public DepositResponse depositFunds(UUID accountId, BigDecimal amount, UUID authenticatedUserId) {
        // ... (validation logic) ...
        validateAmount(amount, "deposit");

        Account account = accountService.getAccountById(accountId, authenticatedUserId);

        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);

        accountService.updateAccount(account);

        Deposit deposit = new Deposit();
        deposit.setAccountId(accountId);
        deposit.setAmount(amount);
        deposit.setTransactionDate(new Date());
        deposit.setDepositId(UUID.randomUUID());
        depositRepository.save(deposit);

        // ... event creation ...

        DepositEvent event = new DepositEvent(deposit.getDepositId(), accountId, amount);
        kafkaTemplate.send("deposit-events", event);

        DepositResponse response = new DepositResponse();
        response.setDepositId(deposit.getDepositId());
        response.setAccountId(deposit.getAccountId());
        response.setAmount(deposit.getAmount());
        response.setTransactionDate(deposit.getTransactionDate());
        return response;
    }

    @Transactional
    public WithdrawalResponse withdrawFunds(UUID accountId, BigDecimal amount, UUID authenticatedUserId) {
        // ... (validation logic) ...
        validateAmount(amount, "withdrawal");

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
        withdrawal.setTransactionDate(new Date());
        withdrawal.setWithdrawalId(UUID.randomUUID());
        withdrawalRepository.save(withdrawal);

        // ... event creation ...

        WithdrawalEvent event = new WithdrawalEvent(withdrawal.getWithdrawalId(), accountId, amount);
        kafkaTemplate.send("withdrawal-events", event);

        WithdrawalResponse response = new WithdrawalResponse();
        response.setWithdrawalId(withdrawal.getWithdrawalId());
        response.setAccountId(withdrawal.getAccountId());
        response.setAmount(withdrawal.getAmount());
        response.setTransactionDate(withdrawal.getTransactionDate());
        return response;
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