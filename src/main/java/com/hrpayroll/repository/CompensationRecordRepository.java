package com.hrpayroll.repository;

import com.hrpayroll.domain.CompensationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompensationRecordRepository extends JpaRepository<CompensationRecord, Long> {
    List<CompensationRecord> findByEmployeeIdOrderByEffectiveDateDesc(Long employeeId);
}
