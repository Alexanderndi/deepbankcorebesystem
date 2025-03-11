package com.ndifreke.core_banking_api.transaction.transactionType.repository;

import com.ndifreke.core_banking_api.transaction.transactionType.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DepositRepository extends JpaRepository<Deposit, UUID> {
    List<Deposit> findByAccountIdOrderByTransactionDateDesc(UUID accountId);
}
