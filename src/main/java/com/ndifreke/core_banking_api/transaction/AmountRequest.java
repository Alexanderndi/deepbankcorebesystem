package com.ndifreke.core_banking_api.transaction;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class AmountRequest {
    // Getters and setters...
    private BigDecimal amount;

}