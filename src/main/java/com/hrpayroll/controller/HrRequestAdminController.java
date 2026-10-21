package com.hrpayroll.controller;

import com.hrpayroll.domain.HrRequest;
import com.hrpayroll.domain.Role;
import com.hrpayroll.dto.CommentForm;
import com.hrpayroll.security.EmployeePrincipal;
import com.hrpayroll.service.EmployeeService;
import com.hrpayroll.service.HrRequestService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** HR admin / manager view of all HR requests, including internal-only notes. */
@Controller
@RequestMapping("/admin/requests")
public class HrRequestAdminController {

    private final HrRequestService hrRequestService;
    private final EmployeeService employeeService;

    public HrRequestAdminController(HrRequestService hrRequestService, EmployeeService employeeService) {
        this.hrRequestService = hrRequestService;
        this.employeeService = employeeService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("requests", hrRequestService.all());
        return "admin/hr-requests";
    }

    @GetMapping("/{id}")
    public String detail(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id, Model model) {
        if (principal.getEmployee().getRole() != Role.HR_ADMIN) {
            throw new AccessDeniedException("HR admin only");
        }
        HrRequest req = hrRequestService.getOrThrow(id);
        model.addAttribute("request", req);
        model.addAttribute("comments", hrRequestService.comments(id, true));
        model.addAttribute("commentForm", new CommentForm());
        model.addAttribute("hrStaff", employeeService.search(null, null, Role.HR_ADMIN));
        return "admin/hr-request-detail";
    }

    @PostMapping("/{id}/comments")
    public String addComment(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id,
                              @ModelAttribute("commentForm") CommentForm form) {
        HrRequest req = hrRequestService.getOrThrow(id);
        hrRequestService.addComment(req, principal.getEmployee(), form, true);
        return "redirect:/admin/requests/" + id;
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam String status) {
        HrRequest req = hrRequestService.getOrThrow(id);
        hrRequestService.changeStatus(req, com.hrpayroll.domain.HrRequestStatus.valueOf(status));
        return "redirect:/admin/requests/" + id;
    }

    @PostMapping("/{id}/assign")
    public String assign(@PathVariable Long id, @RequestParam Long assigneeId) {
        HrRequest req = hrRequestService.getOrThrow(id);
        hrRequestService.assign(req, employeeService.getOrThrow(assigneeId));
        return "redirect:/admin/requests/" + id;
    }

    /**
     * Runs the HR desk assistant over an employee's request thread and applies
     * the triage outcome it returns, so the desk can work the queue without
     * opening every thread by hand.
     */
    @PostMapping("/{id}/triage")
    public String triageWithAssistant(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id, Model model) {
        if (principal.getEmployee().getRole() != Role.HR_ADMIN) {
            throw new AccessDeniedException("HR admin only");
        }
        HrRequest req = hrRequestService.getOrThrow(id);
        StringBuilder thread = new StringBuilder();
        thread.append("Subject: ").append(req.getSubject()).append('\n');
        thread.append("Description: ").append(req.getDescription()).append('\n');
        for (com.hrpayroll.domain.HrRequestComment c : hrRequestService.comments(id, true)) {
            thread.append(c.isInternalOnly() ? "[internal] " : "[reply] ").append(c.getBody()).append('\n');
        }
        hrRequestService.applyAssistantTriage(req, thread.toString(), principal);
        model.addAttribute("requests", hrRequestService.all());
        return "admin/hr-requests";
    }
}
