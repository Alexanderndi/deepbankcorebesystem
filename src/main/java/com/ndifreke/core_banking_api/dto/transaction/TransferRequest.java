package com.ndifreke.core_banking_api.dto.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * The type Transfer request.
 */
@Schema(description = "Transfer request object")
public class TransferRequest {

    @NotNull(message = "Source account ID cannot be null")
    private UUID fromAccountId;

    @NotNull(message = "Destination account ID cannot be null")
    private UUID toAccountId;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    private String description;

    // Getters and setters...

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