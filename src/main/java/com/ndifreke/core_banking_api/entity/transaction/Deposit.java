package com.ndifreke.core_banking_api.entity.transaction;

import com.ndifreke.core_banking_api.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * The type Deposit.
 */
@Entity
@Table(name = "deposits")
@Getter
@Setter
public class Deposit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "deposit_id")
    private UUID depositId;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "transaction_date")
    private Date transactionDate = new Date();

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType; // e.g., Transfer, Deposit, Withdrawal

    // Getters and setters...
}
