package com.ndifreke.core_banking_api.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByFromAccountId(UUID fromAccountId);

    List<Transaction> findByToAccountId(UUID toAccountId);

    List<Transaction> findByFromAccountIdOrToAccountIdOrderByTransactionDateDesc(UUID fromAccountId, UUID toAccountId);
}
