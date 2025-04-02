package com.ndifreke.core_banking_api.transaction.response;

import com.ndifreke.core_banking_api.transaction.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * The type Withdrawal response.
 */
@Schema(description = "Deposit response object")
public class WithdrawalResponse implements TransactionResponseInterface {

    private UUID withdrawalId;
    private UUID accountId;
    private BigDecimal amount;
    private Date transactionDate;
    private TransactionType transactionType;

    // Getters and setters...

    /**
     * Gets withdrawal id.
     *
     * @return the withdrawal id
     */
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
}
