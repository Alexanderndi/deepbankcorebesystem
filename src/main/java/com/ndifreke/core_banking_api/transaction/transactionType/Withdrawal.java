package com.ndifreke.core_banking_api.transaction.transactionType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "withdrawals")
@Getter
@Setter
public class Withdrawal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "withdrawal_id")
    private UUID withdrawalId;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "transaction_date")
    private Date transactionDate = new Date();

    // Getters and setters...
}