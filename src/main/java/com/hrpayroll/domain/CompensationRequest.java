package com.hrpayroll.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "compensation_requests")
public class CompensationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_id")
    private Employee requestedBy;

    @Column(name = "proposed_salary", nullable = false, precision = 14, scale = 2)
    private BigDecimal proposedSalary;

    @Column(name = "proposed_bonus", precision = 14, scale = 2)
    private BigDecimal proposedBonus = BigDecimal.ZERO;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(length = 1000)
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CompensationRequestStatus status = CompensationRequestStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_approved_by_id")
    private Employee managerApprovedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hr_approved_by_id")
    private Employee hrApprovedBy;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Employee getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(Employee requestedBy) {
        this.requestedBy = requestedBy;
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

    public CompensationRequestStatus getStatus() {
        return status;
    }

    public void setStatus(CompensationRequestStatus status) {
        this.status = status;
    }

    public Employee getManagerApprovedBy() {
        return managerApprovedBy;
    }

    public void setManagerApprovedBy(Employee managerApprovedBy) {
        this.managerApprovedBy = managerApprovedBy;
    }

    public Employee getHrApprovedBy() {
        return hrApprovedBy;
    }

    public void setHrApprovedBy(Employee hrApprovedBy) {
        this.hrApprovedBy = hrApprovedBy;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
