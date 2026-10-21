package com.hrpayroll.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "policy_acknowledgements", uniqueConstraints = @UniqueConstraint(columnNames = {"policy_id", "employee_id"}))
public class PolicyAcknowledgement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id")
    private Policy policy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "policy_version", nullable = false)
    private int policyVersion;

    @Column(name = "acknowledged_at", nullable = false)
    private Instant acknowledgedAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public int getPolicyVersion() {
        return policyVersion;
    }

    public void setPolicyVersion(int policyVersion) {
        this.policyVersion = policyVersion;
    }

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(Instant acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }
}
