package com.ndifreke.core_banking_api.account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByUserIdAndAccountType(UUID userId, String accountType);

    List<Account> findByUserId(UUID userId);
}
