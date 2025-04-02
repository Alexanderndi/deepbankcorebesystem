package com.ndifreke.core_banking_api.repository;

import com.ndifreke.core_banking_api.entity.FixedDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * The interface Fixed deposit repository.
 */
@Repository
public interface FixedDepositRepository extends JpaRepository<FixedDeposit, UUID> {
    /**
     * Find by user id list.
     *
     * @param userId the user id
     * @return the list
     */
    List<FixedDeposit> findByUserId(UUID userId);
}
