package com.ndifreke.core_banking_api.transaction.transactionType.repository;

import com.ndifreke.core_banking_api.transaction.transactionType.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {
    List<Transfer> findByFromAccountIdOrToAccountIdOrderByTransactionDateDesc(UUID fromAccountId, UUID toAccountId);
}
