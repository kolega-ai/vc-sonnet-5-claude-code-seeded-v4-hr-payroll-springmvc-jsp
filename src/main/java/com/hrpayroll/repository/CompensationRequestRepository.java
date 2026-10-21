package com.hrpayroll.repository;

import com.hrpayroll.domain.CompensationRequest;
import com.hrpayroll.domain.CompensationRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompensationRequestRepository extends JpaRepository<CompensationRequest, Long> {
    List<CompensationRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    List<CompensationRequest> findByStatusOrderByCreatedAtDesc(CompensationRequestStatus status);

    List<CompensationRequest> findByRequestedByIdOrderByCreatedAtDesc(Long requestedById);

    List<CompensationRequest> findAllByOrderByCreatedAtDesc();
}
