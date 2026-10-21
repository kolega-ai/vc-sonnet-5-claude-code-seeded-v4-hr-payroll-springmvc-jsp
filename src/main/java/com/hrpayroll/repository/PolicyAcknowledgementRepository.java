package com.hrpayroll.repository;

import com.hrpayroll.domain.PolicyAcknowledgement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PolicyAcknowledgementRepository extends JpaRepository<PolicyAcknowledgement, Long> {
    List<PolicyAcknowledgement> findByEmployeeId(Long employeeId);

    Optional<PolicyAcknowledgement> findByPolicyIdAndEmployeeId(Long policyId, Long employeeId);

    List<PolicyAcknowledgement> findAllByOrderByAcknowledgedAtDesc();
}
