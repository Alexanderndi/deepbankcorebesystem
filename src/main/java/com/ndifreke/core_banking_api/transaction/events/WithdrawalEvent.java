package com.ndifreke.core_banking_api.transaction.events;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * The type Withdrawal event.
 */
public class WithdrawalEvent {
    private UUID withdrawalId;
    private UUID accountId;
    private BigDecimal amount;

    /**
     * Instantiates a new Withdrawal event.
     */
// Constructors, getters, setters...
    public WithdrawalEvent(){}

    /**
     * Instantiates a new Withdrawal event.
     *
     * @param withdrawalId the withdrawal id
     * @param accountId    the account id
     * @param amount       the amount
     */
    public WithdrawalEvent(UUID withdrawalId, UUID accountId, BigDecimal amount) {
        this.withdrawalId = withdrawalId;
        this.accountId = accountId;
        this.amount = amount;
    }

    /**
     * Gets withdrawal id.
     *
     * @return the withdrawal id
     */
// getters and setters.
    public UUID getWithdrawalId() {
        return withdrawalId;
    }

    /**
     * Sets withdrawal id.
     *
     * @param withdrawalId the withdrawal id
     */
    public void setWithdrawalId(UUID withdrawalId) {
        this.withdrawalId = withdrawalId;
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