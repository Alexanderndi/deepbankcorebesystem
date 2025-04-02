package com.ndifreke.core_banking_api.account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The interface Account repository.
 */
public interface AccountRepository extends JpaRepository<Account, UUID> {
    /**
     * Find by user id and account type optional.
     *
     * @param userId      the user id
     * @param accountType the account type
     * @return the optional
     */
    Optional<Account> findByUserIdAndAccountType(UUID userId, String accountType);

    /**
     * Find by user id list.
     *
     * @param userId the user id
     * @return the list
     */
    List<Account> findByUserId(UUID userId);
}
