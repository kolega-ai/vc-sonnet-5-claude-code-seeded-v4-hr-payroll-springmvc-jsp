package com.hrpayroll.controller;

import com.hrpayroll.service.PasswordResetService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final PasswordResetService passwordResetService;

    public AuthController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/forgot-password")
    public String forgotForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotSubmit(@RequestParam String email, Model model) {
        passwordResetService.requestReset(email);
        model.addAttribute("submitted", true);
        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetForm(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetSubmit(@RequestParam String token, @RequestParam String password, Model model) {
        boolean ok = passwordResetService.resetPassword(token, password);
        model.addAttribute("token", token);
        model.addAttribute("success", ok);
        return "auth/reset-password";
    }

    public static class ForgotPasswordForm {
        @NotBlank
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
