package com.ndifreke.core_banking_api.dto.savings;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class FixedDepositResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID depositId;
    private BigDecimal depositAmount;
    private LocalDate depositDate;
    private LocalDate maturityDate;
    private BigDecimal interestRate;
    private String status;

    // Getters and setters
    public UUID getDepositId() { return depositId; }
    public void setDepositId(UUID depositId) { this.depositId = depositId; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
    public LocalDate getDepositDate() { return depositDate; }
    public void setDepositDate(LocalDate depositDate) { this.depositDate = depositDate; }
    public LocalDate getMaturityDate() { return maturityDate; }
    public void setMaturityDate(LocalDate maturityDate) { this.maturityDate = maturityDate; }
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}