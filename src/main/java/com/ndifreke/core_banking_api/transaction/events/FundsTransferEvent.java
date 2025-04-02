package com.ndifreke.core_banking_api.transaction.events;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * The type Funds transfer event.
 */
public class FundsTransferEvent {
    private UUID transferId;
    private UUID fromAccountId;
    private UUID toAccountId;
    private BigDecimal amount;

    /**
     * Instantiates a new Funds transfer event.
     */
// Constructors, getters, setters...
    public FundsTransferEvent(){}

    /**
     * Instantiates a new Funds transfer event.
     *
     * @param transferId    the transfer id
     * @param fromAccountId the from account id
     * @param toAccountId   the to account id
     * @param amount        the amount
     */
    public FundsTransferEvent(UUID transferId, UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
        this.transferId = transferId;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
    }

    /**
     * Gets transfer id.
     *
     * @return the transfer id
     */
// getters and setters.
    public UUID getTransferId() {
        return transferId;
    }

    /**
     * Sets transfer id.
     *
     * @param transferId the transfer id
     */
    public void setTransferId(UUID transferId) {
        this.transferId = transferId;
    }

    /**
     * Gets from account id.
     *
     * @return the from account id
     */
    public UUID getFromAccountId() {
        return fromAccountId;
    }

    /**
     * Sets from account id.
     *
     * @param fromAccountId the from account id
     */
    public void setFromAccountId(UUID fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    /**
     * Gets to account id.
     *
     * @return the to account id
     */
    public UUID getToAccountId() {
        return toAccountId;
    }

    /**
     * Sets to account id.
     *
     * @param toAccountId the to account id
     */
    public void setToAccountId(UUID toAccountId) {
        this.toAccountId = toAccountId;
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
