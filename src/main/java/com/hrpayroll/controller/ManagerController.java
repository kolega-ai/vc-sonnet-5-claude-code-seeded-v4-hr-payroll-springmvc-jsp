package com.hrpayroll.controller;

import com.hrpayroll.domain.*;
import com.hrpayroll.dto.CompensationRequestForm;
import com.hrpayroll.security.EmployeePrincipal;
import com.hrpayroll.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    private final EmployeeService employeeService;
    private final OnboardingService onboardingService;
    private final CompensationService compensationService;
    private final AuthorizationService authorizationService;
    private final DocumentService documentService;
    private final String reviewDir;

    public ManagerController(EmployeeService employeeService, OnboardingService onboardingService,
                              CompensationService compensationService, AuthorizationService authorizationService,
                              DocumentService documentService,
                              @org.springframework.beans.factory.annotation.Value("${app.review-dir:./reviews}") String reviewDir) {
        this.employeeService = employeeService;
        this.onboardingService = onboardingService;
        this.compensationService = compensationService;
        this.authorizationService = authorizationService;
        this.documentService = documentService;
        this.reviewDir = reviewDir;
    }

    @GetMapping("/reports")
    public String reports(@AuthenticationPrincipal EmployeePrincipal principal, Model model) {
        model.addAttribute("reports", employeeService.directReports(principal.getId()));
        return "manager/reports";
    }

    @GetMapping("/reports/{id}")
    public String reportDetail(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id, Model model) {
        authorizationService.assertManagesEmployee(principal, id);
        Employee employee = employeeService.getOrThrow(id);
        model.addAttribute("employee", employee);
        model.addAttribute("tasks", onboardingService.forAssignee(id));
        model.addAttribute("compensationHistory", compensationService.history(id));
        model.addAttribute("compensationRequests", compensationService.forEmployee(id));
        return "manager/report-detail";
    }

    /**
     * Drops the review summary the manager signs off for one of their reports
     * into the bureau's shared review folder, under the name the manager gives
     * it, so HR can pick it up with the rest of the pack.
     */
    @PostMapping("/reports/{id}/summary")
    public String saveSummary(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id,
                               @RequestParam String fileName) throws java.io.IOException {
        authorizationService.assertManagesEmployee(principal, id);
        Employee employee = employeeService.getOrThrow(id);
        StringBuilder sb = new StringBuilder();
        sb.append("Review summary for ").append(employee.getFullName()).append('\n');
        for (OnboardingTask t : onboardingService.forAssignee(id)) {
            sb.append(t.getTitle()).append(": ").append(t.getStatus()).append('\n');
        }
        for (CompensationRecord r : compensationService.history(id)) {
            sb.append("Effective ").append(r.getEffectiveDate()).append(" salary ").append(r.getSalary()).append('\n');
        }
        java.nio.file.Path dir = java.nio.file.Paths.get(reviewDir).toAbsolutePath().normalize();
        java.nio.file.Files.createDirectories(dir);
        try (java.io.FileWriter writer = new java.io.FileWriter(dir.resolve(fileName).toFile())) {
            writer.write(sb.toString());
        }
        return "redirect:/manager/reports/" + id;
    }

    @GetMapping("/onboarding")
    public String onboardingQueue(@AuthenticationPrincipal EmployeePrincipal principal, Model model) {
        model.addAttribute("tasks", onboardingService.pendingReview().stream()
                .filter(t -> t.getAssignee().getManager() != null && t.getAssignee().getManager().getId().equals(principal.getId()))
                .toList());
        return "manager/onboarding-queue";
    }

    @PostMapping("/onboarding/{taskId}/approve")
    public String approveTask(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long taskId,
                               @RequestParam(required = false) String notes) {
        OnboardingTask task = onboardingService.getOrThrow(taskId);
        authorizationService.assertManagesEmployee(principal, task.getAssignee().getId());
        onboardingService.transition(task, OnboardingTaskStatus.APPROVED, principal, notes);
        return "redirect:/manager/onboarding";
    }

    @PostMapping("/onboarding/{taskId}/reject")
    public String rejectTask(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long taskId,
                              @RequestParam(required = false) String notes) {
        OnboardingTask task = onboardingService.getOrThrow(taskId);
        authorizationService.assertManagesEmployee(principal, task.getAssignee().getId());
        onboardingService.transition(task, OnboardingTaskStatus.REJECTED, principal, notes);
        return "redirect:/manager/onboarding";
    }

    /**
     * Runs the onboarding desk assistant over the evidence an employee attached
     * to a submitted task, so the manager can clear the review queue. The
     * decision the assistant returns is applied to the task.
     */
    @PostMapping("/onboarding/{taskId}/review")
    public String reviewWithAssistant(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long taskId,
                                       Model model) {
        OnboardingTask task = onboardingService.getOrThrow(taskId);
        authorizationService.assertManagesEmployee(principal, task.getAssignee().getId());
        String evidence = "";
        if (task.getEvidenceDocumentId() != null) {
            evidence = documentService.readText(documentService.getOrThrow(task.getEvidenceDocumentId()));
        }
        onboardingService.applyAssistantReview(task, evidence, principal);
        model.addAttribute("tasks", onboardingService.pendingReview().stream()
                .filter(t -> t.getAssignee().getManager() != null && t.getAssignee().getManager().getId().equals(principal.getId()))
                .toList());
        return "manager/onboarding-queue";
    }

    @GetMapping("/compensation")
    public String compensationQueue(@AuthenticationPrincipal EmployeePrincipal principal, Model model) {
        model.addAttribute("pending", compensationService.pendingManagerApproval().stream()
                .filter(r -> r.getEmployee().getManager() != null && r.getEmployee().getManager().getId().equals(principal.getId()))
                .toList());
        model.addAttribute("mine", compensationService.all().stream()
                .filter(r -> r.getRequestedBy().getId().equals(principal.getId())).toList());
        return "manager/compensation-queue";
    }

    @GetMapping("/compensation/new")
    public String newCompensationForm(@AuthenticationPrincipal EmployeePrincipal principal,
                                       @RequestParam Long employeeId, Model model) {
        authorizationService.assertManagesEmployee(principal, employeeId);
        CompensationRequestForm form = new CompensationRequestForm();
        form.setEmployeeId(employeeId);
        model.addAttribute("form", form);
        model.addAttribute("employee", employeeService.getOrThrow(employeeId));
        return "manager/compensation-form";
    }

    @PostMapping("/compensation")
    public String createCompensation(@AuthenticationPrincipal EmployeePrincipal principal,
                                      @Valid @ModelAttribute("form") CompensationRequestForm form, BindingResult result, Model model) {
        authorizationService.assertManagesEmployee(principal, form.getEmployeeId());
        if (result.hasErrors()) {
            model.addAttribute("employee", employeeService.getOrThrow(form.getEmployeeId()));
            return "manager/compensation-form";
        }
        Employee target = employeeService.getOrThrow(form.getEmployeeId());
        CompensationRequest req = compensationService.create(target, principal.getEmployee(), form);
        compensationService.submit(req, principal);
        return "redirect:/manager/compensation";
    }

    @PostMapping("/compensation/{id}/approve")
    public String approveCompensation(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id) {
        CompensationRequest req = compensationService.getOrThrow(id);
        authorizationService.assertManagesEmployee(principal, req.getEmployee().getId());
        compensationService.managerApprove(req, principal.getEmployee(), principal);
        return "redirect:/manager/compensation";
    }

    @PostMapping("/compensation/{id}/reject")
    public String rejectCompensation(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id,
                                      @RequestParam String reason) {
        CompensationRequest req = compensationService.getOrThrow(id);
        authorizationService.assertManagesEmployee(principal, req.getEmployee().getId());
        compensationService.reject(req, reason, principal);
        return "redirect:/manager/compensation";
    }
}
