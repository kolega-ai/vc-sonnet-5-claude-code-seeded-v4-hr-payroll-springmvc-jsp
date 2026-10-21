package com.hrpayroll.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CompensationRequestForm {

    @NotNull
    private Long employeeId;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal proposedSalary;

    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal proposedBonus = BigDecimal.ZERO;

    @NotNull
    private LocalDate effectiveDate;

    private String justification;

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public BigDecimal getProposedSalary() {
        return proposedSalary;
    }

    public void setProposedSalary(BigDecimal proposedSalary) {
        this.proposedSalary = proposedSalary;
    }

    public BigDecimal getProposedBonus() {
        return proposedBonus;
    }

    public void setProposedBonus(BigDecimal proposedBonus) {
        this.proposedBonus = proposedBonus;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }
}
