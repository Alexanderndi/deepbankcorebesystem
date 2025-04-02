package com.ndifreke.core_banking_api.dto.savings;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * The type Fixed deposit request.
 */
@Data
@Schema(description = "Request object for creating a fixed deposit")
public class FixedDepositRequest {

    @NotNull(message = "Deposit amount is required")
    @Positive(message = "Deposit amount must be positive")
    @Schema(description = "Amount to deposit", example = "5000.00")
    private BigDecimal depositAmount;

    @NotNull(message = "Deposit date is required")
    @Schema(description = "Date of deposit", example = "2024-01-01")
    private LocalDate depositDate;

    @NotNull(message = "Maturity date is required")
    @Schema(description = "Maturity date of the deposit", example = "2025-01-01")
    private LocalDate maturityDate;

    @NotNull(message = "Interest rate is required")
    @Positive(message = "Interest rate must be positive")
    @Schema(description = "Interest rate for the fixed deposit", example = "0.08")
    private BigDecimal interestRate;

    // Getters and Setters
}
