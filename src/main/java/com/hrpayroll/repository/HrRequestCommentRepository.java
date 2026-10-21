package com.hrpayroll.repository;

import com.hrpayroll.domain.HrRequestComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HrRequestCommentRepository extends JpaRepository<HrRequestComment, Long> {
    List<HrRequestComment> findByHrRequestIdOrderByCreatedAtAsc(Long hrRequestId);
}
