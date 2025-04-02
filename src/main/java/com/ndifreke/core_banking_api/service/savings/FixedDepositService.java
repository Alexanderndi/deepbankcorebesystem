package com.ndifreke.core_banking_api.service.savings;

import com.ndifreke.core_banking_api.account.AccountService;
import com.ndifreke.core_banking_api.entity.Account;
import com.ndifreke.core_banking_api.entity.FixedDeposit;
import com.ndifreke.core_banking_api.dto.savings.FixedDepositRequest;
import com.ndifreke.core_banking_api.dto.savings.FixedDepositResponse;
import com.ndifreke.core_banking_api.entity.User;
import com.ndifreke.core_banking_api.entity.enums.savings.FixedDepositStatus;
import com.ndifreke.core_banking_api.repository.FixedDepositRepository;
import com.ndifreke.core_banking_api.repository.UserRepository;
import com.ndifreke.core_banking_api.service.notification.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The type Fixed deposit service.
 */
@Service
public class FixedDepositService {

    @Autowired
    private FixedDepositRepository fixedDepositRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create fixed deposit fixed deposit response.
     *
     * @param request the request
     * @param userId  the user id
     * @return the fixed deposit response
     */
    public FixedDepositResponse createFixedDeposit(FixedDepositRequest request, UUID userId) {
        validateFixedDepositRequest(request);

        // Check if user has a SAVINGS account
        Account savingsAccount = accountService.getUserSavingsAccount(userId);
        if (savingsAccount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User must have a SAVINGS account to create a fixed deposit");
        }

        // Check if SAVINGS account has sufficient balance
        BigDecimal depositAmount = request.getDepositAmount();
        if (savingsAccount.getBalance().compareTo(depositAmount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance in SAVINGS account for deposit");
        }

        FixedDeposit fixedDeposit = new FixedDeposit();
        fixedDeposit.setUserId(userId);
        fixedDeposit.setDepositAmount(request.getDepositAmount());
        fixedDeposit.setDepositDate(request.getDepositDate());
        fixedDeposit.setMaturityDate(request.getMaturityDate());
        fixedDeposit.setInterestRate(request.getInterestRate());
        fixedDeposit.setStatus(FixedDepositStatus.ACTIVE);

        // Debit from SAVINGS account
        accountService.withdrawFromAccount(savingsAccount.getAccountId(), depositAmount);

        // Save the fixed deposit
        FixedDeposit savedDeposit = fixedDepositRepository.save(fixedDeposit);
        // Fetch user details for email
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Send emails
        mailService.sendWithdrawalEmail(
                user.getEmail(),
                user.getFirstName(),
                depositAmount,
                savingsAccount.getAccountNumber()
        );
        mailService.sendDepositEmail(
                user.getEmail(),
                user.getFirstName(),
                depositAmount,
                "Fixed Deposit ID: " + savedDeposit.getDepositId()
        );
        return convertToFixedDepositResponse(savedDeposit);
    }

    /**
     * Gets fixed deposits.
     *
     * @param userId the user id
     * @return the fixed deposits
     */
    public List<FixedDepositResponse> getFixedDeposits(UUID userId) {
        List<FixedDeposit> fixedDeposits = fixedDepositRepository.findByUserId(userId);
        return fixedDeposits.stream().map(this::convertToFixedDepositResponse).collect(Collectors.toList());
    }

    /**
     * Gets fixed deposit by id.
     *
     * @param depositId the deposit id
     * @param userId    the user id
     * @return the fixed deposit by id
     */
    public FixedDepositResponse getFixedDepositById(UUID depositId, UUID userId) {
        FixedDeposit fixedDeposit = fixedDepositRepository.findById(depositId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fixed deposit not found"));
        if (!fixedDeposit.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return convertToFixedDepositResponse(fixedDeposit);
    }

    /**
     * Withdraw fixed deposit fixed deposit response.
     *
     * @param depositId the deposit id
     * @param userId    the user id
     * @return the fixed deposit response
     */
    public FixedDepositResponse withdrawFixedDeposit(UUID depositId, UUID userId) {
        FixedDeposit fixedDeposit = fixedDepositRepository.findById(depositId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fixed deposit not found"));
        if (!fixedDeposit.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        if (fixedDeposit.getStatus() != FixedDepositStatus.MATURED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Fixed deposit not matured");
        }

        // Transfer deposit amount to user's SAVINGS account
        Account savingsAccount = accountService.getUserSavingsAccount(userId);
        if (savingsAccount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User does not have a SAVINGS account for withdrawal");
        }

        BigDecimal withdrawalAmount = fixedDeposit.getDepositAmount();
        accountService.depositToAccount(savingsAccount.getAccountId(), withdrawalAmount);

        fixedDeposit.setStatus(FixedDepositStatus.CLOSED);
        fixedDepositRepository.save(fixedDeposit);

        // Fetch user details for email
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Send withdrawal email
        mailService.sendDepositEmail(
                user.getEmail(),
                user.getFirstName(),
                withdrawalAmount,
                savingsAccount.getAccountNumber()
        );
        return convertToFixedDepositResponse(fixedDeposit);
    }

    private FixedDepositResponse convertToFixedDepositResponse(FixedDeposit fixedDeposit) {
        FixedDepositResponse response = new FixedDepositResponse();
        response.setDepositId(fixedDeposit.getDepositId());
        response.setDepositAmount(fixedDeposit.getDepositAmount());
        response.setDepositDate(fixedDeposit.getDepositDate());
        response.setMaturityDate(fixedDeposit.getMaturityDate());
        response.setInterestRate(fixedDeposit.getInterestRate());
        response.setStatus(fixedDeposit.getStatus().toString());
        return response;
    }

    private void validateFixedDepositRequest(FixedDepositRequest request) {
        LocalDate now = LocalDate.now();
        if (request.getDepositDate().isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deposit date cannot be earlier than today");
        }
        if (request.getDepositAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deposit amount must be greater than zero");
        }
        if (request.getDepositDate().isAfter(request.getMaturityDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deposit date must be before maturity date");
        }
        if (request.getInterestRate().compareTo(BigDecimal.ZERO) < 0 || request.getInterestRate().compareTo(BigDecimal.ONE) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Interest rate must be between 0 and 1");
        }
    }
}