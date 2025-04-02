package com.ndifreke.core_banking_api.entity;

import com.ndifreke.core_banking_api.entity.enums.savings.RecurringDepositFrequency;
import com.ndifreke.core_banking_api.entity.enums.savings.SavingsPlanStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * The type Savings plan.
 */
@Entity
@Data
@Table(name = "savings_plans")
public class SavingsPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "plan_id")
    private UUID planId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(name = "target_amount", nullable = false)
    private BigDecimal targetAmount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "interest_rate", nullable = false)
    private BigDecimal interestRate;

    @Column(name = "recurring_deposit_amount")
    private BigDecimal recurringDepositAmount;

    @Column(name = "recurring_deposit_frequency")
    @Enumerated(EnumType.STRING)
    private RecurringDepositFrequency recurringDepositFrequency;

    @Column(name = "current_balance", nullable = false)
    private BigDecimal currentBalance;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SavingsPlanStatus status; // Enum: ACTIVE, COMPLETED, CLOSED

    // Constructors, Getters, Setters
}
