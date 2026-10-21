package com.hrpayroll.config;

import com.hrpayroll.domain.*;
import com.hrpayroll.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads realistic demo/seed data on first startup so dashboards, filters,
 * exports and workflow queues have enough records to exercise meaningfully.
 * Idempotent: skipped if any employee already exists.
 */
@Component
public class SeedDataRunner implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final CompensationRequestRepository compensationRequestRepository;
    private final CompensationRecordRepository compensationRecordRepository;
    private final OnboardingTaskRepository onboardingTaskRepository;
    private final HrDocumentRepository hrDocumentRepository;
    private final PolicyRepository policyRepository;
    private final PolicyAcknowledgementRepository ackRepository;
    private final HrRequestRepository hrRequestRepository;
    private final HrRequestCommentRepository hrRequestCommentRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean seedEnabled;
    private final String uploadDir;

    public SeedDataRunner(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository,
                           CompensationRequestRepository compensationRequestRepository,
                           CompensationRecordRepository compensationRecordRepository,
                           OnboardingTaskRepository onboardingTaskRepository, HrDocumentRepository hrDocumentRepository,
                           PolicyRepository policyRepository, PolicyAcknowledgementRepository ackRepository,
                           HrRequestRepository hrRequestRepository, HrRequestCommentRepository hrRequestCommentRepository,
                           PasswordEncoder passwordEncoder, @Value("${app.seed.enabled:true}") boolean seedEnabled,
                           @Value("${app.upload-dir}") String uploadDir) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.compensationRequestRepository = compensationRequestRepository;
        this.compensationRecordRepository = compensationRecordRepository;
        this.onboardingTaskRepository = onboardingTaskRepository;
        this.hrDocumentRepository = hrDocumentRepository;
        this.policyRepository = policyRepository;
        this.ackRepository = ackRepository;
        this.hrRequestRepository = hrRequestRepository;
        this.hrRequestCommentRepository = hrRequestCommentRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedEnabled = seedEnabled;
        this.uploadDir = uploadDir;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled || employeeRepository.count() > 0) {
            return;
        }

        String[] deptNames = {"Engineering", "Sales", "Finance", "Customer Support", "People Operations"};
        List<Department> departments = new ArrayList<>();
        for (String name : deptNames) {
            Department d = new Department();
            d.setName(name);
            d.setDescription(name + " department");
            departments.add(departmentRepository.save(d));
        }

        String demoPassword = passwordEncoder.encode("Password123!");

        Employee hrAdmin = newEmployee("hradmin@hrpayroll.example", demoPassword, "Harriet Adams", Role.HR_ADMIN,
                departments.get(4), null, "HR Director", LocalDate.of(2019, 3, 1));

        Employee payroll = newEmployee("payroll@hrpayroll.example", demoPassword, "Paula Ross", Role.PAYROLL_SPECIALIST,
                departments.get(2), hrAdmin, "Payroll Specialist", LocalDate.of(2020, 6, 15));

        List<Employee> managers = new ArrayList<>();
        String[] managerNames = {"Miguel Torres", "Nina Patel", "Owen Clarke", "Priya Nair", "Sam Ibrahim"};
        for (int i = 0; i < 5; i++) {
            Employee m = newEmployee("manager" + (i + 1) + "@hrpayroll.example", demoPassword, managerNames[i],
                    Role.MANAGER, departments.get(i), hrAdmin, "Team Manager", LocalDate.of(2018 + i, 1, 10));
            managers.add(m);
        }

        // Primary demo manager account referenced in the README.
        Employee demoManager = newEmployee("manager@hrpayroll.example", demoPassword, "Morgan Lee", Role.MANAGER,
                departments.get(0), hrAdmin, "Engineering Manager", LocalDate.of(2018, 4, 1));
        managers.add(demoManager);

        List<Employee> employees = new ArrayList<>();
        String[] firstNames = {"Alex", "Bailey", "Casey", "Drew", "Elliot", "Frankie", "Gray", "Harper", "Iris", "Jules",
                "Kai", "Logan", "Mika", "Noor", "Ollie", "Parker", "Quinn", "Riley", "Skyler", "Toni", "Uri", "Val"};
        for (int i = 0; i < firstNames.length; i++) {
            Department dept = departments.get(i % departments.size());
            Employee manager = managers.get(i % managers.size());
            Employee e = newEmployee("employee" + (i + 1) + "@hrpayroll.example", demoPassword,
                    firstNames[i] + " Employee" + (i + 1), Role.EMPLOYEE, dept, manager, "Associate",
                    LocalDate.of(2021, (i % 12) + 1, (i % 27) + 1));
            employees.add(e);
        }
        // Primary demo employee account.
        Employee demoEmployee = newEmployee("employee@hrpayroll.example", demoPassword, "Jamie Rivera", Role.EMPLOYEE,
                departments.get(0), demoManager, "Software Engineer", LocalDate.of(2022, 2, 1));
        employees.add(demoEmployee);

        // Compensation history + requests (10 requests across statuses).
        for (int i = 0; i < employees.size(); i++) {
            Employee e = employees.get(i);
            if (i % 3 == 0) {
                CompensationRecord rec = new CompensationRecord();
                rec.setEmployee(e);
                rec.setSalary(new BigDecimal("68000.00").add(new BigDecimal(i * 500)));
                rec.setBonus(new BigDecimal("2000.00"));
                rec.setEffectiveDate(LocalDate.of(2022, 1, 1));
                compensationRecordRepository.save(rec);
            }
        }
        seedCompensationRequests(employees, managers, hrAdmin);

        // 30 onboarding tasks with varied statuses.
        String[] taskTitles = {"Complete I-9 form", "Submit direct deposit form", "Sign employee handbook",
                "Complete security training", "Set up benefits enrollment", "Upload signed offer letter"};
        int taskCount = 0;
        OnboardingTaskStatus[] statuses = OnboardingTaskStatus.values();
        for (Employee e : employees) {
            for (int t = 0; t < 2 && taskCount < 30; t++, taskCount++) {
                OnboardingTask task = new OnboardingTask();
                task.setAssignee(e);
                task.setTitle(taskTitles[taskCount % taskTitles.length]);
                task.setDescription("Onboarding step for " + e.getFullName());
                task.setDueDate(LocalDate.now().plusDays(7 - (taskCount % 14)));
                task.setStatus(statuses[taskCount % statuses.length]);
                if (task.getStatus() == OnboardingTaskStatus.REJECTED) {
                    task.setReviewNotes("Evidence did not match required document type; please resubmit.");
                    task.setReviewedBy(e.getManager());
                }
                onboardingTaskRepository.save(task);
            }
        }

        // 20 documents, each backed by a real placeholder file on disk so downloads work.
        DocumentCategory[] categories = {DocumentCategory.PAYSLIP, DocumentCategory.TAX_FORM, DocumentCategory.CONTRACT};
        for (int i = 0; i < 20; i++) {
            Employee e = employees.get(i % employees.size());
            String storedName = "seed-placeholder-" + (i + 1) + ".pdf";
            byte[] content = ("Mock document for " + e.getFullName() + " - " + categories[i % categories.length]
                    + " (seed data, not a real file)").getBytes(java.nio.charset.StandardCharsets.UTF_8);
            writeSeedFile(storedName, content);

            HrDocument doc = new HrDocument();
            doc.setEmployee(e);
            doc.setCategory(categories[i % categories.length]);
            doc.setDisplayName(categories[i % categories.length].name().toLowerCase() + "-" + (i + 1) + ".pdf");
            doc.setStoredFilename(storedName);
            doc.setContentType("application/pdf");
            doc.setFileSize(content.length);
            doc.setUploadedBy(payroll);
            hrDocumentRepository.save(doc);
        }

        // 8 policies, some unacknowledged.
        String[] policyTitles = {"Code of Conduct", "Remote Work Policy", "Expense Reimbursement Policy",
                "Data Security Policy", "Anti-Harassment Policy", "Leave Policy", "Equipment Use Policy", "Travel Policy"};
        List<Policy> policies = new ArrayList<>();
        for (String title : policyTitles) {
            Policy p = new Policy();
            p.setTitle(title);
            p.setVersion(1);
            p.setContent("This is the seeded content for the " + title + ". Employees must review and acknowledge.");
            p.setAudience("ALL");
            policies.add(policyRepository.save(p));
        }
        // Acknowledge the first 5 policies for most employees; leave the rest unacknowledged.
        for (int i = 0; i < employees.size(); i++) {
            Employee e = employees.get(i);
            for (int p = 0; p < 5; p++) {
                if ((i + p) % 4 != 0) {
                    PolicyAcknowledgement ack = new PolicyAcknowledgement();
                    ack.setPolicy(policies.get(p));
                    ack.setEmployee(e);
                    ack.setPolicyVersion(1);
                    ackRepository.save(ack);
                }
            }
        }

        // A handful of HR requests with comments.
        String[] subjects = {"Question about benefits enrollment", "Need updated employment verification letter",
                "Payroll discrepancy on last payslip", "Request to update home address", "Parental leave question"};
        for (int i = 0; i < subjects.length; i++) {
            Employee e = employees.get(i);
            HrRequest req = new HrRequest();
            req.setEmployee(e);
            req.setSubject(subjects[i]);
            req.setDescription("Seed data HR request from " + e.getFullName());
            req.setStatus(i % 2 == 0 ? HrRequestStatus.OPEN : HrRequestStatus.WAITING_ON_HR);
            req.setAssignedTo(hrAdmin);
            hrRequestRepository.save(req);

            HrRequestComment c1 = new HrRequestComment();
            c1.setHrRequest(req);
            c1.setAuthor(e);
            c1.setBody("Could you help me with this?");
            hrRequestCommentRepository.save(c1);

            HrRequestComment c2 = new HrRequestComment();
            c2.setHrRequest(req);
            c2.setAuthor(hrAdmin);
            c2.setBody("Internal note: verify employment dates before responding.");
            c2.setInternalOnly(true);
            hrRequestCommentRepository.save(c2);
        }
    }

    private void seedCompensationRequests(List<Employee> employees, List<Employee> managers, Employee hrAdmin) {
        CompensationRequestStatus[] statuses = {
                CompensationRequestStatus.DRAFT, CompensationRequestStatus.SUBMITTED, CompensationRequestStatus.SUBMITTED,
                CompensationRequestStatus.MANAGER_APPROVED, CompensationRequestStatus.MANAGER_APPROVED,
                CompensationRequestStatus.HR_APPROVED, CompensationRequestStatus.EFFECTIVE, CompensationRequestStatus.EFFECTIVE,
                CompensationRequestStatus.REJECTED, CompensationRequestStatus.SUBMITTED
        };
        for (int i = 0; i < statuses.length; i++) {
            Employee target = employees.get(i);
            Employee manager = target.getManager() != null ? target.getManager() : managers.get(0);
            CompensationRequest req = new CompensationRequest();
            req.setEmployee(target);
            req.setRequestedBy(manager);
            req.setProposedSalary(new BigDecimal("72000.00").add(new BigDecimal(i * 750)));
            req.setProposedBonus(new BigDecimal("1500.00"));
            req.setEffectiveDate(LocalDate.now().plusMonths(1));
            req.setJustification("Annual performance-based adjustment.");
            req.setStatus(statuses[i]);
            if (statuses[i].ordinal() >= CompensationRequestStatus.MANAGER_APPROVED.ordinal()
                    && statuses[i] != CompensationRequestStatus.REJECTED) {
                req.setManagerApprovedBy(manager);
            }
            if (statuses[i] == CompensationRequestStatus.HR_APPROVED || statuses[i] == CompensationRequestStatus.EFFECTIVE) {
                req.setHrApprovedBy(hrAdmin);
            }
            if (statuses[i] == CompensationRequestStatus.REJECTED) {
                req.setRejectionReason("Budget constraints for this cycle.");
            }
            compensationRequestRepository.save(req);

            if (statuses[i] == CompensationRequestStatus.EFFECTIVE) {
                CompensationRecord record = new CompensationRecord();
                record.setEmployee(target);
                record.setSalary(req.getProposedSalary());
                record.setBonus(req.getProposedBonus());
                record.setEffectiveDate(req.getEffectiveDate());
                record.setSourceRequest(req);
                compensationRecordRepository.save(record);
            }
        }
    }

    private void writeSeedFile(String storedName, byte[] content) {
        try {
            java.nio.file.Path base = java.nio.file.Paths.get(uploadDir).toAbsolutePath().normalize();
            java.nio.file.Files.createDirectories(base);
            java.nio.file.Path target = base.resolve(storedName).normalize();
            if (!target.startsWith(base)) {
                return;
            }
            java.nio.file.Files.write(target, content);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Failed to write seed document", e);
        }
    }

    private Employee newEmployee(String email, String passwordHash, String fullName, Role role, Department dept,
                                  Employee manager, String jobTitle, LocalDate startDate) {
        Employee e = new Employee();
        e.setEmail(email);
        e.setPasswordHash(passwordHash);
        e.setFullName(fullName);
        e.setRole(role);
        e.setDepartment(dept);
        e.setManager(manager);
        e.setJobTitle(jobTitle);
        e.setStartDate(startDate);
        e.setPhone("555-01" + (Math.abs(email.hashCode()) % 90 + 10));
        e.setAddress("100 Main St, Springfield");
        return employeeRepository.save(e);
    }
}
