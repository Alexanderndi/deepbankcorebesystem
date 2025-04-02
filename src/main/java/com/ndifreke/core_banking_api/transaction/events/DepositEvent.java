package com.ndifreke.core_banking_api.transaction.events;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * The type Deposit event.
 */
public class DepositEvent {
    private UUID depositId;
    private UUID accountId;
    private BigDecimal amount;

    /**
     * Instantiates a new Deposit event.
     */
    public DepositEvent(){}

    /**
     * Instantiates a new Deposit event.
     *
     * @param depositId the deposit id
     * @param accountId the account id
     * @param amount    the amount
     */
    public DepositEvent(UUID depositId, UUID accountId, BigDecimal amount) {
        this.depositId = depositId;
        this.accountId = accountId;
        this.amount = amount;
    }

    /**
     * Gets deposit id.
     *
     * @return the deposit id
     */
    public UUID getDepositId() {
        return depositId;
    }

    /**
     * Sets deposit id.
     *
     * @param depositId the deposit id
     */
    public void setDepositId(UUID depositId) {
        this.depositId = depositId;
    }

    /**
     * Gets account id.
     *
     * @return the account id
     */
    public UUID getAccountId() {
        return accountId;
    }

    /**
     * Sets account id.
     *
     * @param accountId the account id
     */
    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    /**
     * Gets amount.
     *
     * @return the amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Sets amount.
     *
     * @param amount the amount
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

}