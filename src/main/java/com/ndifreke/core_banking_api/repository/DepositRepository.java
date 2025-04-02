package com.ndifreke.core_banking_api.repository;

import com.ndifreke.core_banking_api.entity.transaction.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/**
 * The interface Deposit repository.
 */
public interface DepositRepository extends JpaRepository<Deposit, UUID> {
    /**
     * Find by account id order by transaction date desc list.
     *
     * @param accountId the account id
     * @return the list
     */
    List<Deposit> findByAccountIdOrderByTransactionDateDesc(UUID accountId);
}
