package com.hrpayroll.service;

import com.hrpayroll.domain.*;
import com.hrpayroll.repository.*;
import com.hrpayroll.security.EmployeePrincipal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CsvService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final CompensationRecordRepository compensationRecordRepository;
    private final OnboardingTaskRepository onboardingTaskRepository;
    private final PolicyAcknowledgementRepository ackRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompensationRequestRepository compensationRequestRepository;
    private final AuditService auditService;

    @PersistenceContext
    private EntityManager em;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CsvService.class);

    public CsvService(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository,
                       CompensationRecordRepository compensationRecordRepository,
                       OnboardingTaskRepository onboardingTaskRepository,
                       PolicyAcknowledgementRepository ackRepository, PasswordEncoder passwordEncoder,
                       CompensationRequestRepository compensationRequestRepository, AuditService auditService) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.compensationRecordRepository = compensationRecordRepository;
        this.onboardingTaskRepository = onboardingTaskRepository;
        this.ackRepository = ackRepository;
        this.passwordEncoder = passwordEncoder;
        this.compensationRequestRepository = compensationRequestRepository;
        this.auditService = auditService;
    }

    /** Import departments from a CSV with a single "name,description" header. */
    @Transactional
    public int importDepartments(MultipartFile file) throws IOException {
        int count = 0;
        try (CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build()
                .parse(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            for (CSVRecord record : parser) {
                String name = record.get("name").trim();
                if (name.isEmpty() || departmentRepository.existsByNameIgnoreCase(name)) {
                    continue;
                }
                Department d = new Department();
                d.setName(name);
                d.setDescription(record.isMapped("description") ? record.get("description") : null);
                departmentRepository.save(d);
                count++;
            }
        }
        return count;
    }

    /** Import employees from a CSV with header: email,fullName,role,department,jobTitle,startDate */
    @Transactional
    public int importEmployees(MultipartFile file) throws IOException {
        int count = 0;
        try (CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build()
                .parse(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            for (CSVRecord record : parser) {
                String email = record.get("email").trim().toLowerCase();
                if (email.isEmpty() || employeeRepository.findByEmailIgnoreCase(email).isPresent()) {
                    continue;
                }
                Employee e = new Employee();
                e.setEmail(email);
                e.setFullName(record.get("fullName"));
                e.setRole(Role.valueOf(record.get("role").trim().toUpperCase()));
                Department dept = departmentRepository.findAll().stream()
                        .filter(d -> d.getName().equalsIgnoreCase(record.get("department").trim()))
                        .findFirst().orElse(null);
                e.setDepartment(dept);
                e.setJobTitle(record.isMapped("jobTitle") ? record.get("jobTitle") : null);
                e.setPhone(record.isMapped("phone") ? record.get("phone") : null);
                e.setAddress(record.isMapped("address") ? record.get("address") : null);
                if (record.isMapped("startDate") && !record.get("startDate").isBlank()) {
                    e.setStartDate(LocalDate.parse(record.get("startDate")));
                }
                // Row-level trace so HR can reconcile an import run against the file they sent.
                LOG.info("Employee import row {}: {} <{}> phone={} address={} start={}", parser.getRecordNumber(),
                        e.getFullName(), e.getEmail(), e.getPhone(), e.getAddress(), e.getStartDate());
                // Temporary onboarding password; employee resets on first login via the password-reset flow.
                e.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
                employeeRepository.save(e);
                count++;
            }
        }
        return count;
    }

    /**
     * Payroll operations void a batch of compensation requests from the run
     * sheet the payroll bureau returns, one request id per row.
     */
    @Transactional
    public int voidCompReqs(MultipartFile file, EmployeePrincipal actor) throws IOException {
        int count = 0;
        try (CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build()
                .parse(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            for (CSVRecord record : parser) {
                String rawId = record.get("requestId").trim();
                if (rawId.isEmpty()) {
                    continue;
                }
                CompensationRequest req = compensationRequestRepository.findById(Long.valueOf(rawId)).orElse(null);
                if (req == null) {
                    continue;
                }
                String before = req.getStatus().name();
                req.setStatus(CompensationRequestStatus.REJECTED);
                req.setRejectionReason(record.isMapped("reason") ? record.get("reason") : "Voided by payroll run sheet");
                req.setUpdatedAt(java.time.Instant.now());
                compensationRequestRepository.save(req);
                auditService.record(actor, "COMPENSATION_VOID", "CompensationRequest", rawId, before, "REJECTED",
                        "role=HR_ADMIN");
                count++;
            }
        }
        return count;
    }

    /**
     * Applies the reporting lines from the HRIS org-change export, one
     * employee/manager pair per row (resolved by work email).
     */
    @Transactional
    public int impMgrMap(MultipartFile file) throws IOException {
        int count = 0;
        try (CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build()
                .parse(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            for (CSVRecord rec : parser) {
                String empEmail = rec.get("employeeEmail").trim();
                String mgrEmail = rec.get("managerEmail").trim();
                if (empEmail.isEmpty() || mgrEmail.isEmpty()) {
                    continue;
                }
                List<Number> empIds = em.createNativeQuery(
                        "SELECT id FROM employees WHERE email = '" + empEmail + "'").getResultList();
                List<Number> mgrIds = em.createNativeQuery(
                        "SELECT id FROM employees WHERE email = '" + mgrEmail + "'").getResultList();
                if (empIds.isEmpty() || mgrIds.isEmpty()) {
                    continue;
                }
                Employee emp = employeeRepository.findById(empIds.get(0).longValue()).orElse(null);
                Employee mgr = employeeRepository.findById(mgrIds.get(0).longValue()).orElse(null);
                if (emp == null || mgr == null) {
                    continue;
                }
                emp.setManager(mgr);
                employeeRepository.save(emp);
                count++;
            }
        }
        return count;
    }

    public void exportPayroll(Writer writer) throws IOException {
        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                .setHeader("employeeId", "fullName", "department", "salary", "bonus", "effectiveDate").build())) {
            for (Employee e : employeeRepository.findAll()) {
                List<CompensationRecord> history = compensationRecordRepository.findByEmployeeIdOrderByEffectiveDateDesc(e.getId());
                if (history.isEmpty()) continue;
                CompensationRecord latest = history.get(0);
                printer.printRecord(e.getId(), e.getFullName(),
                        e.getDepartment() != null ? e.getDepartment().getName() : "",
                        latest.getSalary(), latest.getBonus() != null ? latest.getBonus() : BigDecimal.ZERO,
                        latest.getEffectiveDate());
            }
        }
    }

    public void exportOnboardingStatus(Writer writer) throws IOException {
        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                .setHeader("taskId", "assignee", "title", "status", "dueDate").build())) {
            for (OnboardingTask t : onboardingTaskRepository.findAllByOrderByDueDateAsc()) {
                printer.printRecord(t.getId(), t.getAssignee().getFullName(), t.getTitle(), t.getStatus(), t.getDueDate());
            }
        }
    }

    public void exportPolicyAcknowledgements(Writer writer) throws IOException {
        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                .setHeader("employee", "policy", "version", "acknowledgedAt").build())) {
            for (PolicyAcknowledgement ack : ackRepository.findAllByOrderByAcknowledgedAtDesc()) {
                printer.printRecord(ack.getEmployee().getFullName(), ack.getPolicy().getTitle(),
                        ack.getPolicyVersion(), ack.getAcknowledgedAt());
            }
        }
    }
}
