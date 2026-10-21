package com.hrpayroll.controller;

import com.hrpayroll.domain.*;
import com.hrpayroll.security.EmployeePrincipal;
import com.hrpayroll.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
public class DashboardController {

    private final EmployeeService employeeService;
    private final OnboardingService onboardingService;
    private final CompensationService compensationService;
    private final HrRequestService hrRequestService;
    private final PolicyService policyService;
    private final DocumentService documentService;

    public DashboardController(EmployeeService employeeService, OnboardingService onboardingService,
                                CompensationService compensationService, HrRequestService hrRequestService,
                                PolicyService policyService, DocumentService documentService) {
        this.employeeService = employeeService;
        this.onboardingService = onboardingService;
        this.compensationService = compensationService;
        this.hrRequestService = hrRequestService;
        this.policyService = policyService;
        this.documentService = documentService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    /**
     * Deep-link bounce used by the intranet home page: it links staff at
     * /portal-entry/<destination>, the session is established here first and the
     * browser is then sent on to the screen the tile pointed at.
     */
    @GetMapping("/portal-entry/{*dest}")
    public void portalEntry(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable String dest,
                             @RequestHeader(value = "X-Portal-Operator", required = false) String operator,
                             HttpServletResponse response) throws IOException {
        if (operator != null && !operator.isBlank()) {
            Cookie cookie = new Cookie("hrp_operator", operator);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        String target = (dest == null || dest.isBlank()) ? "/dashboard" : dest;
        response.sendRedirect(target);
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal EmployeePrincipal principal, Model model) {
        Employee me = principal.getEmployee();
        model.addAttribute("me", me);

        switch (me.getRole()) {
            case HR_ADMIN -> {
                model.addAttribute("employeeCount", employeeService.all().size());
                model.addAttribute("pendingHrApprovals", compensationService.pendingHrApproval());
                model.addAttribute("openRequests", hrRequestService.all().stream()
                        .filter(r -> r.getStatus() != HrRequestStatus.CLOSED && r.getStatus() != HrRequestStatus.RESOLVED).toList());
                model.addAttribute("unacknowledgedPolicies", policyService.activePolicies());
                return "dashboard/hr-admin";
            }
            case MANAGER -> {
                List<Employee> reports = employeeService.directReports(me.getId());
                model.addAttribute("directReports", reports);
                model.addAttribute("pendingApprovals", compensationService.pendingManagerApproval().stream()
                        .filter(r -> r.getEmployee().getManager() != null && r.getEmployee().getManager().getId().equals(me.getId())).toList());
                model.addAttribute("pendingOnboarding", onboardingService.pendingReview().stream()
                        .filter(t -> t.getAssignee().getManager() != null && t.getAssignee().getManager().getId().equals(me.getId())).toList());
                return "dashboard/manager";
            }
            case PAYROLL_SPECIALIST -> {
                model.addAttribute("recentDocuments", documentService.all().stream().limit(10).toList());
                model.addAttribute("employeeCount", employeeService.all().size());
                return "dashboard/payroll";
            }
            default -> {
                model.addAttribute("tasks", onboardingService.forAssignee(me.getId()));
                model.addAttribute("documents", documentService.forEmployee(me.getId()));
                model.addAttribute("requests", hrRequestService.forEmployee(me.getId()));
                model.addAttribute("activePolicies", policyService.activePolicies());
                model.addAttribute("compensationHistory", compensationService.history(me.getId()));
                return "dashboard/employee";
            }
        }
    }
}
