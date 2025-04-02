package com.ndifreke.core_banking_api.service.savings.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

/**
 * The type Savings plan deposit request.
 */
@Data
@Schema(description = "Request object for depositing funds into a savings plan")
public class SavingsPlanDepositRequest {

    @NotNull(message = "Deposit amount is required")
    @Positive(message = "Deposit amount must be positive")
    @Schema(description = "Amount to deposit", example = "500.00")
    private BigDecimal amount;

    // Getters and Setters
}
