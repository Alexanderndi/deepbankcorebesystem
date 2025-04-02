package com.ndifreke.core_banking_api.service.savings.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * The type Savings plan request.
 */
@Data
@Schema(description = "Request object for creating a savings plan")
public class SavingsPlanRequest {

    @NotBlank(message = "Plan name is required")
    @Schema(description = "Name of the savings plan", example = "My Dream Home")
    private String planName;

    @NotNull(message = "Target amount is required")
    @Positive(message = "Target amount must be positive")
    @Schema(description = "Target amount to save", example = "100000.00")
    private BigDecimal targetAmount;

    @NotNull(message = "Start date is required")
    @Schema(description = "Start date of the savings plan", example = "2024-01-01")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Schema(description = "End date of the savings plan", example = "2025-01-01")
    private LocalDate endDate;

    @NotNull(message = "Interest rate is required")
    @Positive(message = "Interest rate must be positive")
    @Schema(description = "Interest rate for the savings plan", example = "0.05")
    private BigDecimal interestRate;

    @NotNull(message = "Recurring deposit amount is required")
    @Positive(message = "Recurring deposit amount must be positive")
    @Schema(description = "Amount to deposit regularly", example = "1000.00")
    private BigDecimal recurringDepositAmount;

    @NotBlank(message = "Recurring deposit frequency is required")
    @Schema(description = "Frequency of recurring deposits", example = "MONTHLY")
    private String recurringDepositFrequency;

    // Getters and Setters
}
