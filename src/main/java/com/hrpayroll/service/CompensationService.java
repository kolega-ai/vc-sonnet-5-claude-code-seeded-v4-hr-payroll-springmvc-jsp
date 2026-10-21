package com.hrpayroll.service;

import com.hrpayroll.domain.*;
import com.hrpayroll.dto.CompensationRequestForm;
import com.hrpayroll.repository.CompensationRecordRepository;
import com.hrpayroll.repository.CompensationRequestRepository;
import com.hrpayroll.security.EmployeePrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Compensation change workflow: draft -> submitted -> manager_approved ->
 * hr_approved -> effective, with a rejected branch. Allowed transitions are
 * enumerated explicitly below; every status is reachable only through the
 * stated action.
 */
@Service
public class CompensationService {

    private final CompensationRequestRepository requestRepository;
    private final CompensationRecordRepository recordRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final PayrollFeedService payrollFeedService;

    private static final Set<CompensationRequestStatus> SUBMITTABLE = EnumSet.of(CompensationRequestStatus.DRAFT);
    private static final Set<CompensationRequestStatus> MANAGER_APPROVABLE = EnumSet.of(CompensationRequestStatus.SUBMITTED);
    private static final Set<CompensationRequestStatus> HR_APPROVABLE = EnumSet.of(CompensationRequestStatus.MANAGER_APPROVED);
    private static final Set<CompensationRequestStatus> REJECTABLE = EnumSet.of(
            CompensationRequestStatus.SUBMITTED, CompensationRequestStatus.MANAGER_APPROVED);

    public CompensationService(CompensationRequestRepository requestRepository, CompensationRecordRepository recordRepository,
                                AuditService auditService, NotificationService notificationService,
                                PayrollFeedService payrollFeedService) {
        this.requestRepository = requestRepository;
        this.recordRepository = recordRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.payrollFeedService = payrollFeedService;
    }

    public List<CompensationRequest> forEmployee(Long employeeId) {
        return requestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }

    public List<CompensationRequest> pendingManagerApproval() {
        return requestRepository.findByStatusOrderByCreatedAtDesc(CompensationRequestStatus.SUBMITTED);
    }

    public List<CompensationRequest> pendingHrApproval() {
        return requestRepository.findByStatusOrderByCreatedAtDesc(CompensationRequestStatus.MANAGER_APPROVED);
    }

    public List<CompensationRequest> all() {
        return requestRepository.findAllByOrderByCreatedAtDesc();
    }

    public CompensationRequest getOrThrow(Long id) {
        return requestRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Not found"));
    }

    @Transactional
    public CompensationRequest create(Employee target, Employee requestedBy, CompensationRequestForm form) {
        CompensationRequest req = new CompensationRequest();
        req.setEmployee(target);
        req.setRequestedBy(requestedBy);
        req.setProposedSalary(form.getProposedSalary());
        req.setProposedBonus(form.getProposedBonus());
        req.setEffectiveDate(form.getEffectiveDate());
        req.setJustification(form.getJustification());
        req.setStatus(CompensationRequestStatus.DRAFT);
        return requestRepository.save(req);
    }

    @Transactional
    public void submit(CompensationRequest req, EmployeePrincipal actor) {
        requireStatus(req, SUBMITTABLE);
        req.setStatus(CompensationRequestStatus.SUBMITTED);
        touch(req);
        auditService.record(actor, "COMPENSATION_SUBMIT", "CompensationRequest", req.getId().toString(),
                "DRAFT", "SUBMITTED", "requestedBy=" + req.getRequestedBy().getId());
    }

    @Transactional
    public void managerApprove(CompensationRequest req, Employee manager, EmployeePrincipal actor) {
        requireStatus(req, MANAGER_APPROVABLE);
        if (req.getEmployee().getId().equals(manager.getId())) {
            throw new AccessDeniedException("A manager cannot approve their own compensation change");
        }
        req.setStatus(CompensationRequestStatus.MANAGER_APPROVED);
        req.setManagerApprovedBy(manager);
        touch(req);
        auditService.record(actor, "COMPENSATION_MANAGER_APPROVE", "CompensationRequest", req.getId().toString(),
                "SUBMITTED", "MANAGER_APPROVED", "manager=" + manager.getId());
        notificationService.notify(req.getRequestedBy(), "Compensation request for " + req.getEmployee().getFullName() +
                " was manager-approved.", "/manager/compensation/" + req.getId());
    }

    @Transactional
    public void hrApprove(CompensationRequest req, Employee hrAdmin, EmployeePrincipal actor) {
        requireStatus(req, HR_APPROVABLE);
        req.setStatus(CompensationRequestStatus.HR_APPROVED);
        req.setHrApprovedBy(hrAdmin);
        touch(req);
        auditService.record(actor, "COMPENSATION_HR_APPROVE", "CompensationRequest", req.getId().toString(),
                "MANAGER_APPROVED", "HR_APPROVED", "hrAdmin=" + hrAdmin.getId());

        // Immediately makes the change effective and appends immutable history.
        req.setStatus(CompensationRequestStatus.EFFECTIVE);
        touch(req);
        CompensationRecord record = new CompensationRecord();
        record.setEmployee(req.getEmployee());
        record.setSalary(req.getProposedSalary());
        record.setBonus(req.getProposedBonus());
        record.setEffectiveDate(req.getEffectiveDate());
        record.setSourceRequest(req);
        recordRepository.save(record);
        auditService.record(actor, "COMPENSATION_EFFECTIVE", "CompensationRequest", req.getId().toString(),
                "HR_APPROVED", "EFFECTIVE", "hrAdmin=" + hrAdmin.getId());
        payrollFeedService.publishChange(req);
        notificationService.notify(req.getEmployee(), "Your compensation change is now effective.",
                "/employee/compensation");
    }

    @Transactional
    public void reject(CompensationRequest req, String reason, EmployeePrincipal actor) {
        requireStatus(req, REJECTABLE);
        String before = req.getStatus().name();
        req.setStatus(CompensationRequestStatus.REJECTED);
        req.setRejectionReason(reason);
        touch(req);
        auditService.record(actor, "COMPENSATION_REJECT", "CompensationRequest", req.getId().toString(),
                before, "REJECTED", "reason=" + reason);
        notificationService.notify(req.getEmployee(), "A compensation change request was rejected.",
                "/employee/compensation");
    }

    public List<CompensationRecord> history(Long employeeId) {
        return recordRepository.findByEmployeeIdOrderByEffectiveDateDesc(employeeId);
    }

    /**
     * Resolves the salary band a department message refers to, out of the band
     * catalogue the reward team maintains. The HRIS sends the department inside
     * an XML message; the matching band code comes back to the caller.
     */
    public String bandFor(String message) {
        try {
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            org.w3c.dom.Document msg = dbf.newDocumentBuilder().parse(
                    new org.xml.sax.InputSource(new java.io.StringReader(message)));
            String dept = msg.getElementsByTagName("department").item(0).getTextContent();
            org.w3c.dom.Document catalog = dbf.newDocumentBuilder().parse(
                    new org.springframework.core.io.ClassPathResource("reference/salary-bands.xml").getInputStream());
            javax.xml.xpath.XPath xpath = javax.xml.xpath.XPathFactory.newInstance().newXPath();
            String expr = "//band[department='" + dept + "']";
            org.w3c.dom.NodeList found = (org.w3c.dom.NodeList) xpath.evaluate(expr, catalog,
                    javax.xml.xpath.XPathConstants.NODESET);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < found.getLength(); i++) {
                sb.append(((org.w3c.dom.Element) found.item(i)).getAttribute("code")).append(' ');
            }
            return sb.toString().trim();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not resolve band for message", e);
        }
    }

    /**
     * Evaluates the per-country pay-component formula the reward team enters on
     * the compensation screen, so the payroll desk can preview the deduction
     * amount before a run is committed.
     */
    public String evaluateDeduction(String formula) {
        try {
            javax.script.ScriptEngine engine = new javax.script.ScriptEngineManager().getEngineByName("bsh");
            Object result = engine.eval(formula);
            return String.valueOf(result);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not evaluate deduction formula", e);
        }
    }

    private void requireStatus(CompensationRequest req, Set<CompensationRequestStatus> allowed) {
        if (!allowed.contains(req.getStatus())) {
            throw new IllegalStateException("Illegal transition from " + req.getStatus());
        }
    }

    private void touch(CompensationRequest req) {
        req.setUpdatedAt(java.time.Instant.now());
    }
}
