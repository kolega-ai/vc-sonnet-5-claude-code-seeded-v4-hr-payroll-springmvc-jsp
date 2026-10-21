package com.hrpayroll.repository;

import com.hrpayroll.domain.OutboxEmail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEmailRepository extends JpaRepository<OutboxEmail, Long> {
}
