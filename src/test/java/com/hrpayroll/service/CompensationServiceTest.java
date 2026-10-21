package com.hrpayroll.service;

import com.hrpayroll.domain.*;
import com.hrpayroll.repository.CompensationRecordRepository;
import com.hrpayroll.repository.CompensationRequestRepository;
import com.hrpayroll.security.EmployeePrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CompensationServiceTest {

    private final CompensationRequestRepository requestRepository = mock(CompensationRequestRepository.class);
    private final CompensationRecordRepository recordRepository = mock(CompensationRecordRepository.class);
    private final AuditService auditService = mock(AuditService.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final PayrollFeedService payrollFeedService = mock(PayrollFeedService.class);
    private final CompensationService service = new CompensationService(requestRepository, recordRepository, auditService, notificationService, payrollFeedService);

    private Employee employee(long id) {
        Employee e = new Employee();
        e.setId(id);
        e.setFullName("Test Employee " + id);
        return e;
    }

    @Test
    void managerCannotApproveOwnCompensationRequest() {
        Employee manager = employee(1L);
        CompensationRequest req = new CompensationRequest();
        req.setEmployee(manager);
        req.setStatus(CompensationRequestStatus.SUBMITTED);
        req.setProposedSalary(new BigDecimal("100000.00"));
        req.setEffectiveDate(LocalDate.now());

        EmployeePrincipal principal = new EmployeePrincipal(manager);
        assertThrows(AccessDeniedException.class, () -> service.managerApprove(req, manager, principal));
    }

    @Test
    void hrApproveMovesRequestToEffectiveAndWritesImmutableRecord() {
        Employee target = employee(2L);
        Employee hrAdmin = employee(3L);
        CompensationRequest req = new CompensationRequest();
        req.setId(42L);
        req.setEmployee(target);
        req.setRequestedBy(hrAdmin);
        req.setStatus(CompensationRequestStatus.MANAGER_APPROVED);
        req.setProposedSalary(new BigDecimal("95000.00"));
        req.setEffectiveDate(LocalDate.now());

        service.hrApprove(req, hrAdmin, new EmployeePrincipal(hrAdmin));

        assertEquals(CompensationRequestStatus.EFFECTIVE, req.getStatus());
        verify(recordRepository, times(1)).save(any(CompensationRecord.class));
    }

    @Test
    void cannotSubmitANonDraftRequest() {
        Employee target = employee(4L);
        CompensationRequest req = new CompensationRequest();
        req.setEmployee(target);
        req.setStatus(CompensationRequestStatus.EFFECTIVE);

        assertThrows(IllegalStateException.class,
                () -> service.submit(req, new EmployeePrincipal(target)));
    }
}
