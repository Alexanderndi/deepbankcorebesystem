package com.ndifreke.core_banking_api.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@Schema(description = "Amount request object")
public class AmountRequest {
    // Getters and setters...
    private BigDecimal amount;

}