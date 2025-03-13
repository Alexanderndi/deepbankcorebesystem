package com.ndifreke.core_banking_api.transaction.events;

import java.math.BigDecimal;
import java.util.UUID;

public class WithdrawalEvent {
    private UUID withdrawalId;
    private UUID accountId;
    private BigDecimal amount;

    // Constructors, getters, setters...
    public WithdrawalEvent(){}

    public WithdrawalEvent(UUID withdrawalId, UUID accountId, BigDecimal amount) {
        this.withdrawalId = withdrawalId;
        this.accountId = accountId;
        this.amount = amount;
    }

    // getters and setters.
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
}