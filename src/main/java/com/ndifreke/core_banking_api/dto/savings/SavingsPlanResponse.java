package com.ndifreke.core_banking_api.dto.savings;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class SavingsPlanResponse implements Serializable {
    private static final long serialVersionUID = 1L; // Recommended for Serializable classes

    private UUID planId;
    private String planName;
    private BigDecimal targetAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal interestRate;
    private BigDecimal recurringDepositAmount;
    private String recurringDepositFrequency;
    private BigDecimal currentBalance;
    private String status;

    // Getters and setters
    public UUID getPlanId() { return planId; }
    public void setPlanId(UUID planId) { this.planId = planId; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    public BigDecimal getRecurringDepositAmount() { return recurringDepositAmount; }
    public void setRecurringDepositAmount(BigDecimal recurringDepositAmount) { this.recurringDepositAmount = recurringDepositAmount; }
    public String getRecurringDepositFrequency() { return recurringDepositFrequency; }
    public void setRecurringDepositFrequency(String recurringDepositFrequency) { this.recurringDepositFrequency = recurringDepositFrequency; }
    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}