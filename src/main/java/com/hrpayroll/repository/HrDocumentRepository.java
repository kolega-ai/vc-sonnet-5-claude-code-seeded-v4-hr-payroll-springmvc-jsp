package com.hrpayroll.repository;

import com.hrpayroll.domain.DocumentCategory;
import com.hrpayroll.domain.HrDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HrDocumentRepository extends JpaRepository<HrDocument, Long> {
    List<HrDocument> findByEmployeeIdOrderByUploadedAtDesc(Long employeeId);

    List<HrDocument> findByEmployeeIdAndCategoryOrderByUploadedAtDesc(Long employeeId, DocumentCategory category);

    List<HrDocument> findAllByOrderByUploadedAtDesc();
}
