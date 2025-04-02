package com.ndifreke.core_banking_api.service.savings.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * The type Fixed deposit response.
 */
@Data
@Schema(description = "Response object for fixed deposit details")
public class FixedDepositResponse {

    @Schema(description = "Unique ID of the fixed deposit")
    private UUID depositId;

    @Schema(description = "Amount deposited")
    private BigDecimal depositAmount;

    @Schema(description = "Date of deposit")
    private LocalDate depositDate;

    @Schema(description = "Maturity date of the deposit")
    private LocalDate maturityDate;

    @Schema(description = "Interest rate for the fixed deposit")
    private BigDecimal interestRate;

    @Schema(description = "Status of the fixed deposit")
    private String status;

    // Getters and Setters
}
