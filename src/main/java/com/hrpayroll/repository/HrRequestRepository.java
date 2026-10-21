package com.hrpayroll.repository;

import com.hrpayroll.domain.HrRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HrRequestRepository extends JpaRepository<HrRequest, Long> {
    List<HrRequest> findByEmployeeIdOrderByUpdatedAtDesc(Long employeeId);

    List<HrRequest> findAllByOrderByUpdatedAtDesc();
}
