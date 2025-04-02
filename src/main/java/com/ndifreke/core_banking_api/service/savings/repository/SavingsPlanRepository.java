package com.ndifreke.core_banking_api.service.savings.repository;

import com.ndifreke.core_banking_api.service.savings.SavingsPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * The interface Savings plan repository.
 */
@Repository
public interface SavingsPlanRepository extends JpaRepository<SavingsPlan, UUID> {
    /**
     * Find by user id list.
     *
     * @param userId the user id
     * @return the list
     */
    List<SavingsPlan> findByUserId(UUID userId);
}
