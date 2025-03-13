package com.ndifreke.core_banking_api.transaction.events;

import java.math.BigDecimal;
import java.util.UUID;

public class DepositEvent {
    private UUID depositId;
    private UUID accountId;
    private BigDecimal amount;

    public DepositEvent(){}

    public DepositEvent(UUID depositId, UUID accountId, BigDecimal amount) {
        this.depositId = depositId;
        this.accountId = accountId;
        this.amount = amount;
    }

    public UUID getDepositId() {
        return depositId;
    }

    public void setDepositId(UUID depositId) {
        this.depositId = depositId;
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