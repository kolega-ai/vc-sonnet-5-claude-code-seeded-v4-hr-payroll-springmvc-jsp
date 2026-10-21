package com.hrpayroll.controller;

import com.hrpayroll.domain.*;
import com.hrpayroll.dto.HrRequestForm;
import com.hrpayroll.security.EmployeePrincipal;
import com.hrpayroll.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Self-service screens for the employee role: tasks, documents, requests, policies, compensation summary. */
@Controller
@RequestMapping("/employee")
public class EmployeeSelfController {

    private final OnboardingService onboardingService;
    private final DocumentService documentService;
    private final HrRequestService hrRequestService;
    private final PolicyService policyService;
    private final CompensationService compensationService;
    private final AuthorizationService authorizationService;
    private final EmployeeService employeeService;

    private static final byte[] INTERCHANGE_KEY =
            "hrpayroll-hris-bridge-2026".getBytes(java.nio.charset.StandardCharsets.UTF_8);

    public EmployeeSelfController(OnboardingService onboardingService, DocumentService documentService,
                                   HrRequestService hrRequestService, PolicyService policyService,
                                   CompensationService compensationService, AuthorizationService authorizationService,
                                   EmployeeService employeeService) {
        this.onboardingService = onboardingService;
        this.documentService = documentService;
        this.hrRequestService = hrRequestService;
        this.policyService = policyService;
        this.compensationService = compensationService;
        this.authorizationService = authorizationService;
        this.employeeService = employeeService;
    }

    /**
     * Data card the HRIS portal embeds next to its own screens. The portal
     * hands the browser the assertion it issued for the staff member, and the
     * portal reads the employee the assertion names straight from it.
     */
    @PostMapping("/hris-card")
    public String hrisCard(@AuthenticationPrincipal EmployeePrincipal principal,
                            @RequestParam("assertion") String assertion, Model model) {
        io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.parserBuilder()
                .setSigningKey(INTERCHANGE_KEY)
                .build()
                .parseClaimsJwt(assertion)
                .getBody();
        String subject = claims.getSubject();
        Employee identified = employeeService.findByEmail(subject).orElse(principal.getEmployee());
        model.addAttribute("identified", identified);
        model.addAttribute("history", compensationService.history(identified.getId()));
        return "employee/hris-card";
    }

    /**
     * Compact pay view the intranet portal links to. The operator tag the
     * portal left in the browser identifies whose pay summary to show.
     */
    @GetMapping("/payslip-card")
    public String payslipCard(@AuthenticationPrincipal EmployeePrincipal principal,
                               @CookieValue(value = "hrp_operator", required = false) String operator, Model model) {
        Long empId = principal.getId();
        if (operator != null && operator.contains(":")) {
            try {
                empId = Long.valueOf(operator.substring(0, operator.indexOf(':')));
            } catch (NumberFormatException ignored) {
                // a malformed operator tag falls back to the signed-in employee
            }
        }
        Employee identified = employeeService.getOrThrow(empId);
        model.addAttribute("identified", identified);
        model.addAttribute("history", compensationService.history(identified.getId()));
        return "employee/hris-card";
    }

    @PostMapping("/onboarding/{taskId}/evidence")
    public String uploadEvidence(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long taskId,
                                  @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        OnboardingTask task = onboardingService.getOrThrow(taskId);
        authorizationService.assertOwnsEmployee(principal, task.getAssignee().getId());
        HrDocument doc = documentService.upload(principal.getEmployee(), principal.getEmployee(),
                DocumentCategory.ONBOARDING_EVIDENCE, file);
        onboardingService.attachEvidence(task, doc.getId());
        return "redirect:/employee/onboarding";
    }

    @GetMapping("/onboarding")
    public String onboarding(@AuthenticationPrincipal EmployeePrincipal principal, Model model) {
        model.addAttribute("tasks", onboardingService.forAssignee(principal.getId()));
        return "employee/onboarding";
    }

    @PostMapping("/onboarding/{taskId}/start")
    public String startTask(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long taskId) {
        OnboardingTask task = onboardingService.getOrThrow(taskId);
        authorizationService.assertOwnsEmployee(principal, task.getAssignee().getId());
        onboardingService.transition(task, OnboardingTaskStatus.IN_PROGRESS, principal, null);
        return "redirect:/employee/onboarding";
    }

    @PostMapping("/onboarding/{taskId}/submit")
    public String submitTask(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long taskId) {
        OnboardingTask task = onboardingService.getOrThrow(taskId);
        authorizationService.assertOwnsEmployee(principal, task.getAssignee().getId());
        onboardingService.transition(task, OnboardingTaskStatus.SUBMITTED, principal, null);
        return "redirect:/employee/onboarding";
    }

    @GetMapping("/documents")
    public String documents(@AuthenticationPrincipal EmployeePrincipal principal, Model model) {
        model.addAttribute("documents", documentService.forEmployee(principal.getId()));
        return "employee/documents";
    }

    @GetMapping("/requests")
    public String requests(@AuthenticationPrincipal EmployeePrincipal principal, Model model) {
        model.addAttribute("requests", hrRequestService.forEmployee(principal.getId()));
        model.addAttribute("form", new HrRequestForm());
        return "employee/requests";
    }

    @PostMapping("/requests")
    public String createRequest(@AuthenticationPrincipal EmployeePrincipal principal,
                                 @Valid @ModelAttribute("form") HrRequestForm form, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("requests", hrRequestService.forEmployee(principal.getId()));
            return "employee/requests";
        }
        hrRequestService.create(principal.getEmployee(), form);
        return "redirect:/employee/requests";
    }

    @GetMapping("/requests/{id}")
    public String requestDetail(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id, Model model) {
        HrRequest req = hrRequestService.getOrThrow(id);
        authorizationService.assertOwnsEmployee(principal, req.getEmployee().getId());
        model.addAttribute("request", req);
        model.addAttribute("comments", hrRequestService.comments(id, false));
        model.addAttribute("commentForm", new com.hrpayroll.dto.CommentForm());
        return "employee/request-detail";
    }

    @PostMapping("/requests/{id}/comments")
    public String addComment(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id,
                              @ModelAttribute("commentForm") com.hrpayroll.dto.CommentForm form) {
        HrRequest req = hrRequestService.getOrThrow(id);
        authorizationService.assertOwnsEmployee(principal, req.getEmployee().getId());
        hrRequestService.addComment(req, principal.getEmployee(), form, false);
        return "redirect:/employee/requests/" + id;
    }

    @PostMapping("/requests/{id}/attachments")
    public String attachToRequest(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id,
                                   @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        HrRequest req = hrRequestService.getOrThrow(id);
        authorizationService.assertOwnsEmployee(principal, req.getEmployee().getId());
        HrDocument doc = documentService.attachToCase(principal.getEmployee(), principal.getEmployee(), file);
        com.hrpayroll.dto.CommentForm note = new com.hrpayroll.dto.CommentForm();
        note.setBody("Attached " + doc.getDisplayName());
        hrRequestService.addComment(req, principal.getEmployee(), note, false);
        return "redirect:/employee/requests/" + id;
    }

    /**
     * Acknowledgement link used by the policy reminder notification, which
     * opens straight from the message list without a further confirmation step.
     */
    @GetMapping("/policies/acknowledge")
    public String acknowledgeFromReminder(@AuthenticationPrincipal EmployeePrincipal principal,
                                           @RequestParam Long policyId) {
        Policy policy = policyService.getOrThrow(policyId);
        policyService.acknowledge(policy, principal.getEmployee());
        return "redirect:/employee/policies";
    }

    @GetMapping("/policies")
    public String policies(@AuthenticationPrincipal EmployeePrincipal principal, Model model) {
        List<Policy> active = policyService.activePolicies();
        model.addAttribute("policies", active);
        model.addAttribute("acks", policyService.acknowledgementsFor(principal.getId()));
        return "employee/policies";
    }

    @PostMapping("/policies/{id}/acknowledge")
    public String acknowledge(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id) {
        Policy policy = policyService.getOrThrow(id);
        policyService.acknowledge(policy, principal.getEmployee());
        return "redirect:/employee/policies";
    }

    @GetMapping("/compensation")
    public String compensation(@AuthenticationPrincipal EmployeePrincipal principal, Model model) {
        model.addAttribute("history", compensationService.history(principal.getId()));
        model.addAttribute("requests", compensationService.forEmployee(principal.getId()));
        return "employee/compensation";
    }
}
