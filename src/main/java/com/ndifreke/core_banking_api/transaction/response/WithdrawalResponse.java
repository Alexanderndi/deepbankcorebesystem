package com.ndifreke.core_banking_api.transaction.response;

import com.ndifreke.core_banking_api.transaction.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Schema(description = "Deposit response object")
public class WithdrawalResponse implements TransactionResponseInterface {

    private UUID withdrawalId;
    private UUID accountId;
    private BigDecimal amount;
    private Date transactionDate;
    private TransactionType transactionType;

    // Getters and setters...

    public UUID getWithdrawalId() {
        return withdrawalId;
    }

    public void setWithdrawalId(UUID withdrawalId) {
        this.withdrawalId = withdrawalId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
}
