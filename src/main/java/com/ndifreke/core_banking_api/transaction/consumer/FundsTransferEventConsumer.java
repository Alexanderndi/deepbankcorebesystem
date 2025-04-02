package com.ndifreke.core_banking_api.transaction.consumer;

import com.ndifreke.core_banking_api.account.Account;
import com.ndifreke.core_banking_api.account.AccountService;
import com.ndifreke.core_banking_api.notification.NotificationService;
import com.ndifreke.core_banking_api.transaction.events.FundsTransferEvent;
import com.ndifreke.core_banking_api.transaction.transactionType.Transfer;
import com.ndifreke.core_banking_api.transaction.transactionType.repository.TransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * The type Funds transfer event consumer.
 */
@Component
public class FundsTransferEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(FundsTransferEventConsumer.class);

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Consume funds transfer event.
     *
     * @param event the event
     */
    @KafkaListener(topics = "funds-transfer-events", groupId = "core-banking-group")
    public void consumeFundsTransferEvent(FundsTransferEvent event) {
        logger.info("Received funds transfer event: {}", event);

        Account fromAccount = accountService.findAccountById(event.getFromAccountId()).orElseThrow();
        Account toAccount = accountService.findAccountById(event.getToAccountId()).orElseThrow();

        BigDecimal newFromBalance = fromAccount.getBalance().subtract(event.getAmount());
        BigDecimal newToBalance = toAccount.getBalance().add(event.getAmount());

        fromAccount.setBalance(newFromBalance);
        toAccount.setBalance(newToBalance);

        accountService.updateAccount(fromAccount);
        accountService.updateAccount(toAccount);

        Transfer transfer = new Transfer();
        transfer.setTransactionId(event.getTransferId());
        transfer.setFromAccountId(event.getFromAccountId());
        transfer.setToAccountId(event.getToAccountId());
        transfer.setAmount(event.getAmount());
        transferRepository.save(transfer);

        notificationService.sendTransferNotification(event.getFromAccountId(), event.getToAccountId(), event.getAmount());

        logger.info("Funds transfer event processed successfully");
    }
}