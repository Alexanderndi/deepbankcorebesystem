package com.ndifreke.core_banking_api.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * The type Notification service.
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    /**
     * Send deposit notification.
     *
     * @param accountId the account id
     * @param amount    the amount
     */
    public void sendDepositNotification(UUID accountId, BigDecimal amount) {
        logger.info("Deposit notification: Account {} received a deposit of {}", accountId, amount);
    }

    /**
     * Send withdrawal notification.
     *
     * @param accountId the account id
     * @param amount    the amount
     */
    public void sendWithdrawalNotification(UUID accountId, BigDecimal amount) {
        logger.info("Withdrawal notification: Account {} made a withdrawal of {}", accountId, amount);
    }

    /**
     * Send transfer notification.
     *
     * @param fromAccountId the from account id
     * @param toAccountId   the to account id
     * @param amount        the amount
     */
    public void sendTransferNotification(UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
        logger.info("Transfer notification: Account {} transferred {} to account {}", fromAccountId, amount, toAccountId);
    }
}