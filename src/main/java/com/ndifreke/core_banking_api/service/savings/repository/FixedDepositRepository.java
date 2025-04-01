package com.ndifreke.core_banking_api.service.savings.repository;

import com.ndifreke.core_banking_api.service.savings.FixedDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface FixedDepositRepository extends JpaRepository<FixedDeposit, UUID> {
    List<FixedDeposit> findByUserId(UUID userId);
}
