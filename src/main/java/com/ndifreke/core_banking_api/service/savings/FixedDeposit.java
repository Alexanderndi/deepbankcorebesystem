package com.ndifreke.core_banking_api.service.savings;

import com.ndifreke.core_banking_api.service.savings.enums.FixedDepositStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * The type Fixed deposit.
 */
@Entity
@Data
@Table(name = "fixed_deposits")
public class FixedDeposit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "deposit_id")
    private UUID depositId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "deposit_amount", nullable = false)
    private BigDecimal depositAmount;

    @Column(name = "deposit_date", nullable = false)
    private LocalDate depositDate;

    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;

    @Column(name = "interest_rate", nullable = false)
    private BigDecimal interestRate;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private FixedDepositStatus status; // Enum: ACTIVE, MATURED, CLOSED

    // Constructors, Getters, Setters
}
