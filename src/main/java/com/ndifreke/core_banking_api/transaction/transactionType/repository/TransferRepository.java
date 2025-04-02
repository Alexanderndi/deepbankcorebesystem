package com.ndifreke.core_banking_api.transaction.transactionType.repository;

import com.ndifreke.core_banking_api.transaction.transactionType.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/**
 * The interface Transfer repository.
 */
public interface TransferRepository extends JpaRepository<Transfer, UUID> {
    /**
     * Find by from account id or to account id order by transaction date desc list.
     *
     * @param fromAccountId the from account id
     * @param toAccountId   the to account id
     * @return the list
     */
    List<Transfer> findByFromAccountIdOrToAccountIdOrderByTransactionDateDesc(UUID fromAccountId, UUID toAccountId);
}
