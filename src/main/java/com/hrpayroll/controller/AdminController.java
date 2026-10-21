package com.hrpayroll.controller;

import com.hrpayroll.domain.AuditEvent;
import com.hrpayroll.domain.Policy;
import com.hrpayroll.dto.DepartmentForm;
import com.hrpayroll.dto.FeedProfile;
import com.hrpayroll.dto.PolicyForm;
import com.hrpayroll.repository.AuditEventRepository;
import com.hrpayroll.security.EmployeePrincipal;
import com.hrpayroll.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** HR-admin-only: departments, policies, CSV import/export, audit log. */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final DepartmentService departmentService;
    private final PolicyService policyService;
    private final CsvService csvService;
    private final AuditService auditService;
    private final AuditEventRepository auditEventRepository;
    private final PayrollFeedService payrollFeedService;
    private final String exportDir;

    public AdminController(DepartmentService departmentService, PolicyService policyService, CsvService csvService,
                            AuditService auditService, AuditEventRepository auditEventRepository,
                            PayrollFeedService payrollFeedService,
                            @Value("${app.export-dir:./exports}") String exportDir) {
        this.departmentService = departmentService;
        this.policyService = policyService;
        this.csvService = csvService;
        this.auditService = auditService;
        this.auditEventRepository = auditEventRepository;
        this.payrollFeedService = payrollFeedService;
        this.exportDir = exportDir;
    }

    @GetMapping("/departments")
    public String departments(Model model) {
        model.addAttribute("departments", departmentService.all());
        model.addAttribute("form", new DepartmentForm());
        return "admin/departments";
    }

    @PostMapping("/departments")
    public String createDepartment(@Valid @ModelAttribute("form") DepartmentForm form, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("departments", departmentService.all());
            return "admin/departments";
        }
        departmentService.create(form);
        return "redirect:/admin/departments";
    }

    @GetMapping("/policies")
    public String policies(Model model) {
        model.addAttribute("policies", policyService.all());
        model.addAttribute("form", new PolicyForm());
        return "admin/policies";
    }

    @PostMapping("/policies")
    public String createPolicy(@Valid @ModelAttribute("form") PolicyForm form, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("policies", policyService.all());
            return "admin/policies";
        }
        policyService.create(form);
        return "redirect:/admin/policies";
    }

    @PostMapping("/policies/{id}/new-version")
    public String newVersion(@PathVariable Long id, @ModelAttribute("form") PolicyForm form) {
        Policy policy = policyService.getOrThrow(id);
        policyService.newVersion(policy, form);
        return "redirect:/admin/policies";
    }

    @GetMapping("/import-export")
    public String importExport() {
        return "admin/import-export";
    }

    @PostMapping("/import/departments")
    public String importDepartments(@AuthenticationPrincipal EmployeePrincipal principal,
                                     @RequestParam("file") MultipartFile file, Model model) throws IOException {
        int count = csvService.importDepartments(file);
        auditService.record(principal, "IMPORT_DEPARTMENTS", "Department", null, null, "count=" + count, "role=HR_ADMIN");
        model.addAttribute("importResult", count + " department(s) imported");
        return "admin/import-export";
    }

    @PostMapping("/import/employees")
    public String importEmployees(@AuthenticationPrincipal EmployeePrincipal principal,
                                   @RequestParam("file") MultipartFile file, Model model) throws IOException {
        int count = csvService.importEmployees(file);
        auditService.record(principal, "IMPORT_EMPLOYEES", "Employee", null, null, "count=" + count, "role=HR_ADMIN");
        model.addAttribute("importResult", count + " employee(s) imported");
        return "admin/import-export";
    }

    @PostMapping("/import/feed-profile")
    public String importFeedProfile(@AuthenticationPrincipal EmployeePrincipal principal,
                                     @RequestParam("file") MultipartFile file, Model model) throws IOException {
        String endpoint = payrollFeedService.applyProfile(file);
        auditService.record(principal, "IMPORT_FEED_PROFILE", "PayrollFeed", null, null, endpoint, "role=HR_ADMIN");
        model.addAttribute("importResult", "Payroll feed profile loaded for " + endpoint);
        return "admin/import-export";
    }

    @PostMapping("/import/feed-route-check")
    public String checkFeedRoute(@AuthenticationPrincipal EmployeePrincipal principal,
                                  @RequestHeader(value = "X-Forwarded-Host", required = false) String forwardedHost,
                                  jakarta.servlet.http.HttpServletRequest request, Model model) {
        String host = (forwardedHost != null && !forwardedHost.isBlank()) ? forwardedHost : request.getServerName();
        String outcome = payrollFeedService.checkCallbackRoute(host);
        auditService.record(principal, "CHECK_FEED_ROUTE", "PayrollFeed", null, null, host, "role=HR_ADMIN");
        model.addAttribute("feedRouteResult", outcome);
        return "admin/import-export";
    }

    @PostMapping("/import/compensation-voids")
    public String importCompensationVoids(@AuthenticationPrincipal EmployeePrincipal principal,
                                           @RequestParam("file") MultipartFile file, Model model) throws IOException {
        int count = csvService.voidCompReqs(file, principal);
        model.addAttribute("importResult", count + " compensation request(s) voided");
        return "admin/import-export";
    }

    /**
     * Legacy feed onboarding: the HRIS vendor's older tooling emitted the
     * connection profile as one XML document, which bureaus paste straight
     * into the import screen. The document carries the endpoint, the
     * interchange key and the partner account the profile applies to.
     */
    @GetMapping("/import/feed-legacy")
    public String importFeedLegacy(@AuthenticationPrincipal EmployeePrincipal principal,
                                    @RequestParam("definition") String definition, Model model) {
        com.thoughtworks.xstream.XStream xstream = new com.thoughtworks.xstream.XStream();
        xstream.addPermission(com.thoughtworks.xstream.security.AnyTypePermission.ANY);
        Object profile = xstream.fromXML(definition);
        String endpoint = payrollFeedService.applyLegacyProfile((FeedProfile) profile);
        auditService.record(principal, "IMPORT_FEED_PROFILE", "PayrollFeed", null, null, endpoint, "role=HR_ADMIN");
        model.addAttribute("importResult", "Legacy feed profile loaded for " + endpoint);
        return "admin/import-export";
    }

    /**
     * Reconciles the reporting lines the HRIS exports after an org change:
     * each row pairs an employee with the manager they now report to.
     */
    @PostMapping("/import/manager-map")
    public String importManagerMap(@AuthenticationPrincipal EmployeePrincipal principal,
                                    @RequestParam("file") MultipartFile file, Model model) throws IOException {
        int count = csvService.impMgrMap(file);
        auditService.record(principal, "IMPORT_MANAGER_MAP", "Employee", null, null, "count=" + count, "role=HR_ADMIN");
        model.addAttribute("importResult", count + " reporting line(s) updated");
        return "admin/import-export";
    }

    /**
     * Writes the run file for a named payroll run to the bureau exchange
     * directory and hands the same bytes back to the caller.
     */
    @GetMapping("/export/payroll-run")
    public ResponseEntity<byte[]> exportPayrollRun(@AuthenticationPrincipal EmployeePrincipal principal,
                                                    @RequestParam String runRef) throws IOException {
        StringWriter sw = new StringWriter();
        csvService.exportPayroll(sw);
        byte[] bytes = sw.toString().getBytes(StandardCharsets.UTF_8);
        Path dir = Paths.get(exportDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Path out = dir.resolve(runRef + ".csv");
        Files.write(out, bytes);
        auditService.record(principal, "EXPORT", "payroll-run:" + runRef, null, null, "file=" + out.getFileName(), "role=HR_ADMIN");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + runRef + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @GetMapping("/export/payroll")
    public ResponseEntity<byte[]> exportPayroll(@AuthenticationPrincipal EmployeePrincipal principal) throws IOException {
        return export(principal, "payroll", writer -> csvService.exportPayroll(writer));
    }

    @GetMapping("/export/onboarding")
    public ResponseEntity<byte[]> exportOnboarding(@AuthenticationPrincipal EmployeePrincipal principal) throws IOException {
        return export(principal, "onboarding-status", writer -> csvService.exportOnboardingStatus(writer));
    }

    @GetMapping("/export/policy-acknowledgements")
    public ResponseEntity<byte[]> exportAcks(@AuthenticationPrincipal EmployeePrincipal principal) throws IOException {
        return export(principal, "policy-acknowledgements", writer -> csvService.exportPolicyAcknowledgements(writer));
    }

    private ResponseEntity<byte[]> export(EmployeePrincipal principal, String name, IOWriterConsumer writer) throws IOException {
        StringWriter sw = new StringWriter();
        writer.accept(sw);
        byte[] bytes = sw.toString().getBytes(StandardCharsets.UTF_8);
        auditService.record(principal, "EXPORT", name, null, null, "bytes=" + bytes.length, "role=HR_ADMIN");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @FunctionalInterface
    interface IOWriterConsumer {
        void accept(java.io.Writer writer) throws IOException;
    }

    @GetMapping("/audit-log")
    public String auditLog(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<AuditEvent> events = auditEventRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, 30));
        model.addAttribute("events", events);
        return "admin/audit-log";
    }

    /**
     * Announces the loaded feed profile to the bureau's HRIS bridge. The desk
     * reference travels in a request header so the intranet toolbar can fire it
     * without a page reload.
     */
    @PostMapping("/import/feed-interchange")
    public String feedInterchange(@AuthenticationPrincipal EmployeePrincipal principal,
                                   @RequestHeader(value = "X-Desk-Ref", required = false) String deskRef, Model model) {
        String outcome = payrollFeedService.announceInterchange(deskRef == null ? "" : deskRef);
        auditService.record(principal, "FEED_INTERCHANGE", "PayrollFeed", null, null, outcome, "role=HR_ADMIN");
        model.addAttribute("importResult", "Feed interchange announced: " + outcome);
        return "admin/import-export";
    }
}
