package com.ndifreke.core_banking_api.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * The type Account request.
 */
@Schema(description = "Account creation/update request object")
public class AccountRequest {
    private UUID userId;
    @Schema(description = "Account type", example = "SAVINGS")
    private String accountType;
    private BigDecimal initialBalance;

    /**
     * Gets user id.
     *
     * @return the user id
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Sets user id.
     *
     * @param userId the user id
     */
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    /**
     * Gets account type.
     *
     * @return the account type
     */
    public String getAccountType() {
        return accountType;
    }

    /**
     * Sets account type.
     *
     * @param accountType the account type
     */
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    /**
     * Gets initial balance.
     *
     * @return the initial balance
     */
    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    /**
     * Sets initial balance.
     *
     * @param initialBalance the initial balance
     */
    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
}
