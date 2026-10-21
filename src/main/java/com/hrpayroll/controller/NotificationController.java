package com.hrpayroll.controller;

import com.hrpayroll.domain.Notification;
import com.hrpayroll.security.EmployeePrincipal;
import com.hrpayroll.service.NotificationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public String list(@AuthenticationPrincipal EmployeePrincipal principal, Model model) {
        model.addAttribute("notifications", notificationService.forEmployee(principal.getId()));
        return "notifications/list";
    }

    @PostMapping("/notifications/{id}/read")
    public String markRead(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id) {
        Notification n = notificationService.forEmployee(principal.getId()).stream()
                .filter(x -> x.getId().equals(id)).findFirst()
                .orElseThrow(() -> new AccessDeniedException("Not your notification"));
        notificationService.markRead(n);
        return "redirect:/notifications";
    }
}
