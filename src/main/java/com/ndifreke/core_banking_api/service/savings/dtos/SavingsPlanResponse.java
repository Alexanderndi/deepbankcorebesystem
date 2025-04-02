package com.ndifreke.core_banking_api.service.savings.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * The type Savings plan response.
 */
@Data
@Schema(description = "Response object for savings plan details")
public class SavingsPlanResponse {

    @Schema(description = "Unique ID of the savings plan")
    private UUID planId;

    @Schema(description = "Name of the savings plan")
    private String planName;

    @Schema(description = "Target amount to save")
    private BigDecimal targetAmount;

    @Schema(description = "Start date of the savings plan")
    private LocalDate startDate;

    @Schema(description = "End date of the savings plan")
    private LocalDate endDate;

    @Schema(description = "Interest rate for the savings plan")
    private BigDecimal interestRate;

    @Schema(description = "Amount to deposit regularly")
    private BigDecimal recurringDepositAmount;

    @Schema(description = "Frequency of recurring deposits")
    private String recurringDepositFrequency;

    @Schema(description = "Current balance of the savings plan")
    private BigDecimal currentBalance;

    @Schema(description = "Status of the savings plan")
    private String status;

}