package com.hrpayroll.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Append-only compensation history. Rows are never updated or deleted;
 * a new row is inserted whenever a compensation request becomes EFFECTIVE.
 */
@Entity
@Table(name = "compensation_records")
public class CompensationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "salary", nullable = false, precision = 14, scale = 2)
    private BigDecimal salary;

    @Column(name = "bonus", precision = 14, scale = 2)
    private BigDecimal bonus = BigDecimal.ZERO;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_request_id")
    private CompensationRequest sourceRequest;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

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

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public BigDecimal getBonus() {
        return bonus;
    }

    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public CompensationRequest getSourceRequest() {
        return sourceRequest;
    }

    public void setSourceRequest(CompensationRequest sourceRequest) {
        this.sourceRequest = sourceRequest;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
