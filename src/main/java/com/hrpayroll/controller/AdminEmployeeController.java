package com.hrpayroll.controller;

import com.hrpayroll.domain.*;
import com.hrpayroll.dto.EmployeeHrEditForm;
import com.hrpayroll.dto.RoleChangeForm;
import com.hrpayroll.security.EmployeePrincipal;
import com.hrpayroll.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** HR-admin-only employee, compensation-approval, and role-management screens. */
@Controller
@RequestMapping("/admin")
public class AdminEmployeeController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final CompensationService compensationService;
    private final OnboardingService onboardingService;
    private final DocumentService documentService;
    private final HrRequestService hrRequestService;
    private final AuditService auditService;
    private final AuthorizationService authorizationService;
    private final PasswordResetService passwordResetService;
    private final EmployeeDirectoryService employeeDirectoryService;

    public AdminEmployeeController(EmployeeService employeeService, DepartmentService departmentService,
                                    CompensationService compensationService, OnboardingService onboardingService,
                                    DocumentService documentService, HrRequestService hrRequestService,
                                    AuditService auditService, AuthorizationService authorizationService,
                                    PasswordResetService passwordResetService,
                                    EmployeeDirectoryService employeeDirectoryService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
        this.compensationService = compensationService;
        this.onboardingService = onboardingService;
        this.documentService = documentService;
        this.hrRequestService = hrRequestService;
        this.auditService = auditService;
        this.authorizationService = authorizationService;
        this.passwordResetService = passwordResetService;
        this.employeeDirectoryService = employeeDirectoryService;
    }

    @GetMapping("/employees")
    public String list(@AuthenticationPrincipal EmployeePrincipal principal,
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) Long departmentId,
                        @RequestParam(required = false) Role role, Model model) {
        var results = employeeService.search(q, departmentId, role);
        auditService.recordRead(principal, "Employee", results.size(), "role=HR_ADMIN");
        model.addAttribute("employees", results);
        model.addAttribute("departments", departmentService.all());
        model.addAttribute("roles", Role.values());
        model.addAttribute("q", q);
        return "admin/employee-list";
    }

    @GetMapping("/employees/{id}")
    public String detail(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id, Model model) {
        Employee employee = employeeService.getOrThrow(id);
        auditService.record(principal, "VIEW_EMPLOYEE_PROFILE", "Employee", id.toString(), null, null, "role=HR_ADMIN");
        EmployeeHrEditForm form = new EmployeeHrEditForm();
        form.setFullName(employee.getFullName());
        form.setJobTitle(employee.getJobTitle());
        form.setDepartmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null);
        form.setManagerId(employee.getManager() != null ? employee.getManager().getId() : null);
        form.setEmploymentStatus(employee.getEmploymentStatus().name());
        form.setStartDate(employee.getStartDate());
        form.setPhone(employee.getPhone());
        form.setAddress(employee.getAddress());

        model.addAttribute("employee", employee);
        model.addAttribute("form", form);
        model.addAttribute("departments", departmentService.all());
        model.addAttribute("managers", employeeService.search(null, null, Role.MANAGER));
        model.addAttribute("compensationHistory", compensationService.history(id));
        model.addAttribute("compensationRequests", compensationService.forEmployee(id));
        model.addAttribute("tasks", onboardingService.forAssignee(id));
        model.addAttribute("documents", documentService.forEmployee(id));
        model.addAttribute("requests", hrRequestService.forEmployee(id));
        model.addAttribute("roleForm", new RoleChangeForm());
        return "admin/employee-detail";
    }

    @PostMapping("/employees/{id}")
    public String update(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id,
                          @Valid @ModelAttribute("form") EmployeeHrEditForm form, BindingResult result, Model model) {
        Employee employee = employeeService.getOrThrow(id);
        if (result.hasErrors()) {
            model.addAttribute("employee", employee);
            model.addAttribute("departments", departmentService.all());
            return "admin/employee-detail";
        }
        String before = employee.getEmploymentStatus().name();
        employeeService.updateHrFields(employee, form);
        auditService.record(principal, "UPDATE_EMPLOYEE", "Employee", id.toString(), before, form.getEmploymentStatus(), "role=HR_ADMIN");
        return "redirect:/admin/employees/" + id;
    }

    @PostMapping("/employees/{id}/role")
    public String changeRole(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id,
                              @ModelAttribute("roleForm") RoleChangeForm form) {
        Employee employee = employeeService.getOrThrow(id);
        String before = employee.getRole().name();
        Role newRole = Role.valueOf(form.getRole());
        employeeService.changeRole(employee, newRole);
        auditService.record(principal, "ROLE_CHANGE", "Employee", id.toString(), before, newRole.name(), "role=HR_ADMIN");
        return "redirect:/admin/employees/" + id;
    }

    /**
     * Applies a reporting-line change from the org-change ticket the HR desk
     * is working. The ticket reference travels in a request header so the
     * desk's toolbar can post the change without reopening the profile form.
     */
    @PostMapping("/employees/{id}/manager")
    public String reassignManager(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id,
                                   @RequestHeader(value = "X-Org-Change", required = false) String orgChange,
                                   @RequestParam Long managerId, RedirectAttributes attributes) {
        Employee employee = employeeService.getOrThrow(id);
        Employee manager = employeeService.getOrThrow(managerId);
        employeeService.reassignManager(employee, manager);
        attributes.addFlashAttribute("orgChangeRef", orgChange);
        return "redirect:/admin/employees/" + id;
    }

    @PostMapping("/employees/{id}/enabled")
    public String setEnabled(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id,
                              @RequestParam boolean enabled) {
        Employee employee = employeeService.getOrThrow(id);
        employeeService.setEnabled(employee, enabled);
        auditService.record(principal, enabled ? "ENABLE_ACCOUNT" : "DISABLE_ACCOUNT", "Employee", id.toString(),
                null, String.valueOf(enabled), "role=HR_ADMIN");
        return "redirect:/admin/employees/" + id;
    }

    @PostMapping("/employees/{id}/recovery-link")
    public String employeeRecoveryLink(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id,
                                        RedirectAttributes attributes) {
        Employee employee = employeeService.getOrThrow(id);
        String link = passwordResetService.issueRecoveryLink(employee);
        auditService.record(principal, "ISSUE_RECOVERY_LINK", "Employee", id.toString(), null, null, "role=HR_ADMIN");
        attributes.addFlashAttribute("recoveryLink", link);
        return "redirect:/admin/employees/" + id;
    }

    @PostMapping("/employees/{id}/temp-access")
    public String employeeTempAccess(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id,
                                      RedirectAttributes attributes) {
        Employee employee = employeeService.getOrThrow(id);
        passwordResetService.issueTempAccess(employee);
        auditService.record(principal, "ISSUE_TEMP_ACCESS", "Employee", id.toString(), null, null, "role=HR_ADMIN");
        attributes.addFlashAttribute("tempAccessIssued", true);
        return "redirect:/admin/employees/" + id;
    }

    @PostMapping("/compensation/{id}/approve")
    public String hrApprove(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id) {
        CompensationRequest req = compensationService.getOrThrow(id);
        compensationService.hrApprove(req, principal.getEmployee(), principal);
        return "redirect:/admin/compensation";
    }

    @PostMapping("/compensation/{id}/reject")
    public String hrReject(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id,
                            @RequestParam String reason) {
        CompensationRequest req = compensationService.getOrThrow(id);
        compensationService.reject(req, reason, principal);
        return "redirect:/admin/compensation";
    }

    @GetMapping("/compensation")
    public String compensationQueue(Model model) {
        model.addAttribute("pending", compensationService.pendingHrApproval());
        model.addAttribute("all", compensationService.all());
        return "admin/compensation-queue";
    }

    /**
     * Shows the corporate staff-directory entry the org-change import is
     * based on, so HR can check it against the employee record before applying
     * a reporting-line change.
     */
    @GetMapping("/employees/staff-card")
    public String staffCard(@RequestParam String uid, Model model) {
        model.addAttribute("uid", uid);
        model.addAttribute("staffEntry", employeeDirectoryService.staffEntry(uid));
        return "admin/staff-directory";
    }

    /**
     * Resolves the salary band the HRIS refers to in a pay-message, from the
     * reward team's band catalogue.
     */
    @PostMapping("/compensation/bands/resolve")
    @ResponseBody
    public String resolveBand(@RequestBody String message) {
        return compensationService.bandFor(message);
    }

    /**
     * Previews the per-country pay-component formula the reward team enters on
     * the compensation screen, so they can confirm the deduction amount before
     * it is attached to a country's pay run.
     */
    @PostMapping("/compensation/deductions/preview")
    @ResponseBody
    public String previewDeduction(@RequestBody java.util.Map<String, String> body) {
        return compensationService.evaluateDeduction(body.get("formula"));
    }
}
