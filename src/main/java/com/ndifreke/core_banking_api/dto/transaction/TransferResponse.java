package com.ndifreke.core_banking_api.dto.transaction;

import com.ndifreke.core_banking_api.entity.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * The type Transfer response.
 */
@Schema(description = "Transaction response object")
public class TransferResponse implements TransactionResponseInterface {

    private UUID transactionId;
    private UUID fromAccountId;
    private UUID toAccountId;
    private BigDecimal amount;
    private Date transactionDate;
    private TransactionType transactionType;
    private String description;

    // Getters and setters...

    /**
     * Gets transaction id.
     *
     * @return the transaction id
     */
    public UUID getTransactionId() {
        return transactionId;
    }

    /**
     * Sets transaction id.
     *
     * @param transactionId the transaction id
     */
    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
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

    /**
     * Gets transaction date.
     *
     * @return the transaction date
     */
    public Date getTransactionDate() {
        return transactionDate;
    }

    /**
     * Sets transaction date.
     *
     * @param transactionDate the transaction date
     */
    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    /**
     * Gets transaction type.
     *
     * @return the transaction type
     */
    public TransactionType getTransactionType() {
        return transactionType;
    }

    /**
     * Sets transaction type.
     *
     * @param transactionType the transaction type
     */
    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
