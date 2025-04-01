package com.ndifreke.core_banking_api.service.savings.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "Request object for withdrawing funds from a savings plan")
public class SavingsPlanWithdrawalRequest {

    @NotNull(message = "Withdrawal amount is required")
    @Positive(message = "Withdrawal amount must be positive")
    @Schema(description = "Amount to withdraw", example = "200.00")
    private BigDecimal amount;

    // Getters and Setters
}