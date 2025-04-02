package com.ndifreke.core_banking_api.transaction.consumer;

import com.ndifreke.core_banking_api.entity.Account;
import com.ndifreke.core_banking_api.account.AccountService;
import com.ndifreke.core_banking_api.service.notification.NotificationService;
import com.ndifreke.core_banking_api.transaction.events.DepositEvent;
import com.ndifreke.core_banking_api.entity.transaction.Deposit;
import com.ndifreke.core_banking_api.repository.DepositRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * The type Deposit event consumer.
 */
@Component
public class DepositEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DepositEventConsumer.class);

    @Autowired
    private AccountService accountService;

    @Autowired
    private DepositRepository depositRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Consume deposit event.
     *
     * @param event the event
     */
    @KafkaListener(topics = "deposit-events", groupId = "core-banking-group")
    public void consumeDepositEvent(DepositEvent event) {
        logger.info("Received deposit event: {}", event);

        Account account = accountService.findAccountById(event.getAccountId()).orElseThrow();
        BigDecimal newBalance = account.getBalance().add(event.getAmount());
        account.setBalance(newBalance);
        accountService.updateAccount(account);

        Deposit deposit = new Deposit();
        deposit.setDepositId(event.getDepositId());
        deposit.setAccountId(event.getAccountId());
        deposit.setAmount(event.getAmount());
        depositRepository.save(deposit);

        notificationService.sendDepositNotification(event.getAccountId(), event.getAmount());

        logger.info("Deposit event processed successfully");
    }
}