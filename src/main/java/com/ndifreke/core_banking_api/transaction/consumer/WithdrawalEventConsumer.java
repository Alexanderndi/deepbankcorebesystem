package com.ndifreke.core_banking_api.transaction.consumer;

import com.ndifreke.core_banking_api.account.Account;
import com.ndifreke.core_banking_api.account.AccountService;
import com.ndifreke.core_banking_api.notification.NotificationService;
import com.ndifreke.core_banking_api.transaction.events.WithdrawalEvent;
import com.ndifreke.core_banking_api.transaction.transactionType.Withdrawal;
import com.ndifreke.core_banking_api.transaction.transactionType.repository.WithdrawalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class WithdrawalEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(WithdrawalEventConsumer.class);

    @Autowired
    private AccountService accountService;

    @Autowired
    private WithdrawalRepository withdrawalRepository;

    @Autowired
    private NotificationService notificationService;

    @KafkaListener(topics = "withdrawal-events", groupId = "core-banking-group")
    public void consumeWithdrawalEvent(WithdrawalEvent event) {
        logger.info("Received withdrawal event: {}", event);

        Account account = accountService.findAccountById(event.getAccountId()).orElseThrow();
        BigDecimal newBalance = account.getBalance().subtract(event.getAmount());
        account.setBalance(newBalance);
        accountService.updateAccount(account);

        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setWithdrawalId(event.getWithdrawalId());
        withdrawal.setAccountId(event.getAccountId());
        withdrawal.setAmount(event.getAmount());
        withdrawalRepository.save(withdrawal);

        notificationService.sendWithdrawalNotification(event.getAccountId(), event.getAmount());

        logger.info("Withdrawal event processed successfully");
    }
}