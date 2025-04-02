package com.ndifreke.core_banking_api.service.savings;

import com.ndifreke.core_banking_api.account.AccountService;
import com.ndifreke.core_banking_api.entity.Account;
import com.ndifreke.core_banking_api.entity.SavingsPlan;
import com.ndifreke.core_banking_api.dto.savings.SavingsPlanRequest;
import com.ndifreke.core_banking_api.dto.savings.SavingsPlanResponse;
import com.ndifreke.core_banking_api.entity.User;
import com.ndifreke.core_banking_api.entity.enums.savings.RecurringDepositFrequency;
import com.ndifreke.core_banking_api.entity.enums.savings.SavingsPlanStatus;
import com.ndifreke.core_banking_api.repository.SavingsPlanRepository;
import com.ndifreke.core_banking_api.repository.UserRepository;
import com.ndifreke.core_banking_api.service.notification.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The type Savings plan service.
 */
@Service
public class SavingsPlanService {

    @Autowired
    private SavingsPlanRepository savingsPlanRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create savings plan savings plan response.
     *
     * @param request the request
     * @param userId  the user id
     * @return the savings plan response
     */
    public SavingsPlanResponse createSavingsPlan(SavingsPlanRequest request, UUID userId) {
        validateSavingsPlanRequest(request);

        // Check if user has a SAVINGS account
        Account savingsAccount = accountService.getUserSavingsAccount(userId);
        if (savingsAccount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User must have a SAVINGS account to create a savings plan");
        }

        SavingsPlan savingsPlan = new SavingsPlan();
        savingsPlan.setUserId(userId);
        savingsPlan.setPlanName(request.getPlanName());
        savingsPlan.setTargetAmount(request.getTargetAmount());
        savingsPlan.setStartDate(request.getStartDate());
        savingsPlan.setEndDate(request.getEndDate());
        savingsPlan.setInterestRate(request.getInterestRate());
        savingsPlan.setRecurringDepositAmount(request.getRecurringDepositAmount());
        savingsPlan.setRecurringDepositFrequency(RecurringDepositFrequency.valueOf(request.getRecurringDepositFrequency()));
        savingsPlan.setCurrentBalance(BigDecimal.ZERO);
        savingsPlan.setStatus(SavingsPlanStatus.ACTIVE);

        SavingsPlan savedPlan = savingsPlanRepository.save(savingsPlan);
        return convertToSavingsPlanResponse(savedPlan);
    }

    /**
     * Gets savings plans.
     *
     * @param userId the user id
     * @return the savings plans
     */
    public List<SavingsPlanResponse> getSavingsPlans(UUID userId) {
        List<SavingsPlan> savingsPlans = savingsPlanRepository.findByUserId(userId);
        return savingsPlans.stream().map(this::convertToSavingsPlanResponse).collect(Collectors.toList());
    }

    /**
     * Gets savings plan by id.
     *
     * @param planId the plan id
     * @param userId the user id
     * @return the savings plan by id
     */
    public SavingsPlanResponse getSavingsPlanById(UUID planId, UUID userId) {
        SavingsPlan savingsPlan = savingsPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Savings plan not found"));
        if (!savingsPlan.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return convertToSavingsPlanResponse(savingsPlan);
    }

    /**
     * Deposit to savings plan savings plan response.
     *
     * @param planId the plan id
     * @param amount the amount
     * @param userId the user id
     * @return the savings plan response
     */
    public SavingsPlanResponse depositToSavingsPlan(UUID planId, BigDecimal amount, UUID userId) {
        SavingsPlan savingsPlan = savingsPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Savings plan not found"));
        if (!savingsPlan.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deposit amount must be greater than zero");
        }
        if (savingsPlan.getStatus() != SavingsPlanStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot deposit to an inactive savings plan");
        }

        BigDecimal newBalance = savingsPlan.getCurrentBalance().add(amount);
        savingsPlan.setCurrentBalance(newBalance);
        savingsPlanRepository.save(savingsPlan);
        return convertToSavingsPlanResponse(savingsPlan);
    }

    /**
     * Withdraw from savings plan savings plan response.
     *
     * @param planId the plan id
     * @param amount the amount
     * @param userId the user id
     * @return the savings plan response
     */
    public SavingsPlanResponse withdrawFromSavingsPlan(UUID planId, BigDecimal amount, UUID userId) {
        SavingsPlan savingsPlan = savingsPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Savings plan not found"));
        if (!savingsPlan.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Withdrawal amount must be greater than zero");
        }
        if (savingsPlan.getStatus() != SavingsPlanStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot withdraw from an inactive savings plan");
        }
        if (savingsPlan.getCurrentBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance");
        }

        // Transfer withdrawn amount to user's SAVINGS account
        Account savingsAccount = accountService.getUserSavingsAccount(userId);
        if (savingsAccount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User does not have a SAVINGS account for withdrawal");
        }
        accountService.depositToAccount(savingsAccount.getAccountId(), amount);


        BigDecimal newBalance = savingsPlan.getCurrentBalance().subtract(amount);
        savingsPlan.setCurrentBalance(newBalance);
        savingsPlanRepository.save(savingsPlan);
        return convertToSavingsPlanResponse(savingsPlan);
    }

    private SavingsPlanResponse convertToSavingsPlanResponse(SavingsPlan savingsPlan) {
        SavingsPlanResponse response = new SavingsPlanResponse();
        response.setPlanId(savingsPlan.getPlanId());
        response.setPlanName(savingsPlan.getPlanName());
        response.setTargetAmount(savingsPlan.getTargetAmount());
        response.setStartDate(savingsPlan.getStartDate());
        response.setEndDate(savingsPlan.getEndDate());
        response.setInterestRate(savingsPlan.getInterestRate());
        response.setRecurringDepositAmount(savingsPlan.getRecurringDepositAmount());
        response.setRecurringDepositFrequency(savingsPlan.getRecurringDepositFrequency().toString());
        response.setCurrentBalance(savingsPlan.getCurrentBalance());
        response.setStatus(savingsPlan.getStatus().toString());
        return response;
    }

    private void validateSavingsPlanRequest(SavingsPlanRequest request) {
        LocalDate now = LocalDate.now();
        if (request.getStartDate().isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be earlier than today");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be before end date");
        }
        if (request.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target amount must be greater than zero");
        }
        if (request.getInterestRate().compareTo(BigDecimal.ZERO) < 0 || request.getInterestRate().compareTo(BigDecimal.ONE) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Interest rate must be between 0 and 1");
        }
        if (request.getRecurringDepositAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recurring deposit amount must be greater than zero");
        }
        try {
            RecurringDepositFrequency.valueOf(request.getRecurringDepositFrequency());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid recurring deposit frequency");
        }
    }
}