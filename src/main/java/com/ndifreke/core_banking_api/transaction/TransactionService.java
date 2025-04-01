package com.ndifreke.core_banking_api.transaction;

import com.ndifreke.core_banking_api.account.Account;
import com.ndifreke.core_banking_api.account.AccountService;
import com.ndifreke.core_banking_api.notification.MailService;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private UserRepository userRepository;

    @Transactional
    public TransferResponse transferFunds(UUID fromAccountId, UUID toAccountId, BigDecimal amount, String description, UUID authenticatedUserId) {
        validateAmount(amount, "transfer");
        logger.info("Transfer request: fromAccountId={}, toAccountId={}, amount={}, description={}, authenticatedUserId={}",
                fromAccountId, toAccountId, amount, description, authenticatedUserId);

        Account fromAccount = accountService.getAccountById(fromAccountId, authenticatedUserId);
        Account toAccount = accountService.findAccountById(toAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination account not found"));

        if (fromAccountId == null || toAccountId == null) {
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

        Transfer transfer = new Transfer();
        transfer.setFromAccountId(fromAccountId);
        transfer.setToAccountId(toAccountId);
        transfer.setAmount(amount);
        transfer.setTransactionDate(new Date());
        transfer.setTransactionType(TransactionType.TRANSFER);
        transfer.setDescription(description);
        transferRepository.save(transfer);

        // Send Email Notification
        User fromUser = userRepository.findById(fromAccount.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User associated with source account not found"));
        User toUser = userRepository.findById(toAccount.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User associated with destination account not found"));
        String subject = "Transfer Successful";
        String text = String.format(
                "<html><body>" +
                        "<p>Dear %s,</p>" +
                        "<p>A transfer of <b>%.2f</b> has been debited from your account (%s) to account (%s).</p>" +
                        "<p>Description: %s</p>" +
                        "</body></html>",
                fromUser.getFirstName(), amount, fromAccount.getAccountNumber(), toAccount.getAccountNumber(), description);
        mailService.sendTransactionEmail(fromUser.getEmail(), subject, text);

        String subject2 = "Transfer Successful";
        String creditText = String.format(
                "<html><body>" +
                        "<p>Dear %s,</p>" +
                        "<p>A transfer of <b>%.2f</b> has been credited to your account (%s).</p>" +
                        "<p>Description: %s</p>" +
                        "</body></html>",
                toUser.getFirstName(), amount, toAccount.getAccountNumber(), fromAccount.getAccountNumber(), description);
        mailService.sendTransactionEmail(toUser.getEmail(), subject2, creditText);

        logger.info("Transfer successful: fromAccountId={}, toAccountId={}, amount={}, description={}",
                fromAccountId, toAccountId, amount, description);
        return convertToTransferResponse(transfer);
    }

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User associated with account not found"));
        String subject = "Deposit Successful";
        String text = String.format(
                "<html><body>" +
                        "<p>Dear %s,</p>" +
                        "<p>A deposit of <b>%.2f</b> has been credited to your account (%s).</p>" +
                        "</body></html>",
                user.getFirstName(), amount, account.getAccountNumber());
        mailService.sendTransactionEmail(user.getEmail(), subject, text);

        logger.info("Deposit successful: accountId={}, amount={}", accountId, amount);
        return convertToDepositResponse(deposit);
    }

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User associated with account not found"));
        String subject = "Withdrawal Successful";
        String text = String.format(
                "<html><body>" +
                        "<p>Dear %s,</p>" +
                        "<p>A withdrawal of <b>%.2f</b> has been debited from your account (%s).</p>" +
                        "</body></html>",
                user.getFirstName(), amount, account.getAccountNumber());
        mailService.sendTransactionEmail(user.getEmail(), subject, text);

        logger.info("Withdrawal successful: accountId={}, amount={}", accountId, amount);
        return convertToWithdrawalResponse(withdrawal);
    }

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
        TransferResponse response = new TransferResponse();
        response.setTransactionId(transfer.getTransactionId());
        response.setFromAccountId(transfer.getFromAccountId());
        response.setToAccountId(transfer.getToAccountId());
        response.setAmount(transfer.getAmount());
        response.setTransactionType(transfer.getTransactionType());
        response.setTransactionDate(transfer.getTransactionDate());
        response.setDescription(transfer.getDescription());
        return response;
    }

    private DepositResponse convertToDepositResponse(Deposit deposit) {
        DepositResponse response = new DepositResponse();
        response.setDepositId(deposit.getDepositId());
        response.setAccountId(deposit.getAccountId());
        response.setAmount(deposit.getAmount());
        response.setTransactionDate(deposit.getTransactionDate());
        response.setTransactionType(deposit.getTransactionType());
        return response;
    }

    private WithdrawalResponse convertToWithdrawalResponse(Withdrawal withdrawal) {
        WithdrawalResponse response = new WithdrawalResponse();
        response.setWithdrawalId(withdrawal.getWithdrawalId());
        response.setAccountId(withdrawal.getAccountId());
        response.setAmount(withdrawal.getAmount());
        response.setTransactionType(withdrawal.getTransactionType());
        response.setTransactionDate(withdrawal.getTransactionDate());
        return response;
    }
}