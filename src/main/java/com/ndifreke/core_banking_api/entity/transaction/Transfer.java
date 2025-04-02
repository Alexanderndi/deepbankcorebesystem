package com.ndifreke.core_banking_api.entity.transaction;


import com.ndifreke.core_banking_api.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * The type Transfer.
 */
@Entity
@Getter
@Setter
@Data
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "transaction_id", columnDefinition = "BINARY(16)")
    private UUID transactionId;

    @Column(name = "from_account_id", columnDefinition = "BINARY(16)", nullable = true)
    private UUID fromAccountId;

    @Column(name = "to_account_id", columnDefinition = "BINARY(16)", nullable = true)
    private UUID toAccountId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String description;

    @Column(name = "account_id", columnDefinition = "BINARY(16)", nullable = true) // Add this line
    private UUID accountId;

    private Date transactionDate = new Date();

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType; // e.g., Transfer, Deposit, Withdrawal

    /**
     * Pre persist.
     */
    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }
}
