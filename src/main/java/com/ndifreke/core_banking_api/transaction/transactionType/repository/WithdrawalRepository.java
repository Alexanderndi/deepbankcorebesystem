package com.ndifreke.core_banking_api.transaction.transactionType.repository;

import com.ndifreke.core_banking_api.transaction.transactionType.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/**
 * The interface Withdrawal repository.
 */
public interface WithdrawalRepository extends JpaRepository<Withdrawal, UUID> {
    /**
     * Find by account id order by transaction date desc list.
     *
     * @param accountId the account id
     * @return the list
     */
    List<Withdrawal> findByAccountIdOrderByTransactionDateDesc(UUID accountId);
}
